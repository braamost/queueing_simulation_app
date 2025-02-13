package com.back.Controllers;

import com.back.Configuration.SimulationStateEvent;
import com.back.DTO.*;
import com.back.Observer.Machine;
import com.back.Observer.Process;
import com.back.Observer.Queue;
import com.back.Singleton.PausingMechanism;
import com.back.SnapShot.SimulationHistory;
import com.back.SnapShot.SimulationMemento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

@Service
public class SimulationService {
    private final Map<String, Queue> queues = new HashMap<>();
    private final Map<String, Machine> machines = new HashMap<>();
    private final ApplicationEventPublisher eventPublisher;
    private final PausingMechanism pausingMechanism = PausingMechanism.getInstance();
    private final SimulationHistory history;
    private SimulationStateDTO currentState = new SimulationStateDTO();
    private volatile boolean replaying = false;

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
        replaying = true;
        // initial state
        SimulationMemento prev = history.get();
        eventPublisher.publishEvent(new SimulationStateEvent(this, prev.getState()));

    // replay
        // get first state
        prev = history.get();
        long startTime = prev.getState().getCurrentTime();
        // loop through history
        long endTime = 0;
        while (size >= 0 && replaying){
            SimulationMemento memento = history.get();
            if(memento != null) {
                // replay the state
                currentState = memento.getState();
                endTime = currentState.getCurrentTime();
                eventPublisher.publishEvent(new SimulationStateEvent(this, prev.getState()));

                // sleep till next state
                try{
                    Thread.sleep((endTime - startTime));
                }catch (Exception e) {
                    System.out.println("help");
                }

                // move to next state
                startTime = endTime;
                prev = memento;
            }else {
                // replay last state
                eventPublisher.publishEvent(new SimulationStateEvent(this, prev.getState()));

                //end replay
                SimulationStateDTO state = new SimulationStateDTO();
                state.setType("endReplay");
                eventPublisher.publishEvent(new SimulationStateEvent(this, state));
            }
            size--;
        }
    }

    public void handleEndReplay() {
        SimulationStateDTO state = new SimulationStateDTO();
        state.setType("endReplay");
        replaying = false;
        eventPublisher.publishEvent(new SimulationStateEvent(this, state));

    }

    public void pauseSimulation() {
        pausingMechanism.pause();
    }

    public void resumeSimulation() {
        pausingMechanism.resume();
    }

    public Map<String, Queue> getQueues() {
        return queues;
    }

    public Map<String, Machine> getMachines() {
        return machines;
    }
}