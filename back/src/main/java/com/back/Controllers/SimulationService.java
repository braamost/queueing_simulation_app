package com.back.Controllers;

import com.back.Configuration.SimulationStateEvent;
import com.back.DTO.*;
import com.back.Observer.Machine;
import com.back.Observer.Queue;
import com.back.Singleton.PausingMechanism;
import com.back.SnapShot.SimulationHistory;
import com.back.SnapShot.SimulationMemento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SimulationService {
    private final Map<String, Queue> queues = new HashMap<>();
    private final Map<String, Machine> machines = new HashMap<>();
    private final ApplicationEventPublisher eventPublisher;
    private final PausingMechanism pausingMechanism = PausingMechanism.getInstance();
    private final SimulationHistory history;
    private SimulationStateDTO currentState = new SimulationStateDTO();

    @Autowired
    public SimulationService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.history = new SimulationHistory();
    }

    public void saveState() {
        history.add(new SimulationMemento(currentState));
    }


    public SimulationStateDTO getCurrentState() {
        return currentState;
    }

    public void setCurrentState(SimulationStateDTO state) {
        this.currentState = state;
    }

    public synchronized void updateMachineState(SimulationStateDTO.MachineStateDTO state) {
        SimulationStateDTO simulationState = getCurrentState();
        simulationState.getMachineStates().put(state.getId(), state);
        simulationState.setCurrentTime(System.currentTimeMillis());
        setCurrentState(simulationState);
        saveState();
        eventPublisher.publishEvent(new SimulationStateEvent(this, simulationState));
    }

    public synchronized void updateQueueState(SimulationStateDTO.QueueStateDTO state) {
        SimulationStateDTO simulationState = getCurrentState();
        simulationState.getQueueStates().put(state.getId(), state);
        simulationState.setCurrentTime(System.currentTimeMillis());
        setCurrentState(simulationState);
        saveState();
        eventPublisher.publishEvent(new SimulationStateEvent(this, simulationState));
    }

    public void initializeSimulation(CanvasData canvasData) {
        queues.clear();
        machines.clear();
        history.clear();

        SimulationStateDTO initialState = new SimulationStateDTO();

        // Create queues first
        for (QueueDTO queueDTO : canvasData.getQueues()) {
            Queue queue = new Queue(queueDTO.getId(), queueDTO.getNProcesses(), this);
            queues.put(queueDTO.getId(), queue);
        }

        // Create machines
        for (MachineDTO machineDTO : canvasData.getMachines()) {
            Queue nextQueue = null;
            if (machineDTO.getQueueId() != null && !machineDTO.getQueueId().isEmpty()) {
                nextQueue = queues.get(machineDTO.getQueueId());
            }
            Machine machine = new Machine(machineDTO.getId(), nextQueue, this);
            machines.put(machineDTO.getId(), machine);
            SimulationStateDTO.MachineStateDTO state = new SimulationStateDTO.MachineStateDTO(machineDTO.getId(), true, null, 0, null);
            initialState.getMachineStates().put(machineDTO.getId(), state);
        }

        // Connect machines to queues
        for (QueueDTO queueDTO : canvasData.getQueues()) {
            Queue queue = queues.get(queueDTO.getId());
            for (String machineId : queueDTO.getMachineIds()) {
                Machine machine = machines.get(machineId);
                if (machine != null) {
                    queue.connectMachine(machine);
                }
            }
            SimulationStateDTO.QueueStateDTO state = new SimulationStateDTO.QueueStateDTO(queueDTO.getId(), queue.getProcessCount());
            initialState.getQueueStates().put(queueDTO.getId(), state);
        }

        // save initial state to history
        initialState.setCurrentTime(System.currentTimeMillis());
        setCurrentState(initialState);
        saveState();
    }

    public void updateQueueProcessCount(String queueId, int newCount) {
        Queue queue = queues.get(queueId);
        if (queue != null) {
            queue.updateProcessCount(newCount);
        }
    }

    public synchronized void stopSimulation() {
        // Stop all machines
        for (Machine machine : machines.values()) {
            machine.stop();
        }

        // Clear all queues
        for (Queue queue : queues.values()) {
            queue.clearProcesses();
        }

        // Clear history
        history.clear();

        // Log the stop action
        System.out.println("Simulation stopped: All machines and queues reset.");
    }

    public void replaySimulation() {
        int size = history.size();
        pausingMechanism.replay();

        // Initial state
        SimulationMemento prev = history.get();
        eventPublisher.publishEvent(new SimulationStateEvent(this, prev.getState()));

        prev = history.get();
        if (prev == null) {
            handleEndReplay();
            return;
        }

        // Replay
        long startTime = prev.getState().getCurrentTime();
        long endTime = 0;

        while (size >= 0 && pausingMechanism.isReplaying()) {
            // Check replay status at the start of each iteration
            if (!pausingMechanism.isReplaying()) {
                handleEndReplay();
                return;
            }

            SimulationMemento memento = history.get();
            if (memento != null) {
                // Replay the state
                currentState = memento.getState();
                endTime = currentState.getCurrentTime();
                eventPublisher.publishEvent(new SimulationStateEvent(this, currentState));

                // Sleep in smaller intervals to check for replay interruption
                long sleepDuration = endTime - startTime;
                long sleepInterval = 10; // Sleep in 10ms intervals
                long remainingSleep = sleepDuration;

                while (remainingSleep > 0) {
                    // Check replay status before each sleep interval
                    if (!pausingMechanism.isReplaying()) {
                        handleEndReplay();
                        return;
                    }

                    try {
                        Thread.sleep(Math.min(sleepInterval, remainingSleep));
                    } catch (InterruptedException e) {
                        handleEndReplay();
                        return;
                    }
                    remainingSleep -= sleepInterval;
                }

                startTime = endTime;
                prev = memento;
            } else {
                // Check replay status before publishing final state
                if (!pausingMechanism.isReplaying()) {
                    handleEndReplay();
                    return;
                }
                eventPublisher.publishEvent(new SimulationStateEvent(this, prev.getState()));
                break;
            }
            size--;
        }

        handleEndReplay();
    }

    public void handleEndReplay() {
        // Only send endReplay event if we were actually replaying
        if (pausingMechanism.isReplaying()) {
            SimulationStateDTO state = new SimulationStateDTO();
            state.setType("endReplay");
            pausingMechanism.stopReplay();
            eventPublisher.publishEvent(new SimulationStateEvent(this, state));
        }
    }

    public void pauseSimulation() {
        pausingMechanism.pause();
    }

    public void resumeSimulation() {
        pausingMechanism.resume();
    }
}