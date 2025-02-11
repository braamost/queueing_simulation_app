package com.back.Controllers;

import com.back.Configuration.SimulationStateEvent;
import com.back.DTO.*;
import com.back.Observer.Machine;
import com.back.Observer.Process;
import com.back.Observer.Queue;
import com.back.Singleton.PausingMechanism;
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

    @Autowired
    public SimulationService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public synchronized void updateMachineState(SimulationStateDTO.MachineStateDTO state) {
        SimulationStateDTO simulationState = new SimulationStateDTO();
        simulationState.getMachineStates().put(state.getId(), state);
        eventPublisher.publishEvent(new SimulationStateEvent(this, simulationState));
    }

    public synchronized void updateQueueState(SimulationStateDTO.QueueStateDTO state) {
        SimulationStateDTO simulationState = new SimulationStateDTO();
        simulationState.getQueueStates().put(state.getId(), state);
        eventPublisher.publishEvent(new SimulationStateEvent(this, simulationState));
    }

    public void initializeSimulation(CanvasData canvasData) {
        queues.clear();
        machines.clear();

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
        }
    }

    public void updateQueueProcessCount(String queueId, int newCount) {
        Queue queue = queues.get(queueId);
        if (queue != null) {
            queue.updateProcessCount(newCount);
        }
    }

    public void stopSimulation() {
        // Stop all machines
        for (Machine machine : machines.values()) {
            machine.stop();
        }

        // Clear all queues
        for (Queue queue : queues.values()) {
            queue.clearProcesses();
        }

        // Log the stop action
        System.out.println("Simulation stopped: All machines and queues reset.");
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