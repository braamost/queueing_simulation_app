package com.back.Controllers;

import com.back.DTO.CanvasData;
import com.back.DTO.MachineDTO;
import com.back.DTO.QueueDTO;
import com.back.Observer.Machine;
import com.back.Observer.Process;
import com.back.Observer.Queue;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

///////


import com.back.DTO.CanvasData;
import com.back.DTO.MachineDTO;
import com.back.DTO.QueueDTO;
import com.back.Observer.Machine;
import com.back.Observer.Process;
import com.back.Observer.Queue;
import com.back.snapShot.SimulationMemento;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimulationService {
    private final Map<String, Queue> queues = new HashMap<>();
    private final Map<String, Machine> machines = new HashMap<>();
    private SimulationMemento simulationMemento = new SimulationMemento(); // Store the snapshot
    private CanvasData canvasData; // Store the
    public void initializeSimulation(CanvasData canvasData) {
        this.canvasData = canvasData;
        for (QueueDTO queueDTO : canvasData.getQueues()) {
            Queue queue = new Queue(queueDTO.getId(), queueDTO.getNProcesses());
            queues.put(queueDTO.getId(), queue);
        }

        // Create machines
        for (MachineDTO machineDTO : canvasData.getMachines()) {
            Queue nextQueue = null;
            if (!"".equals(machineDTO.getQueueId())) {
                nextQueue = queues.get(machineDTO.getQueueId());
            }
            Machine machine = new Machine(machineDTO.getId(), nextQueue);
            machines.put(machineDTO.getId(), machine);
        }

        // Connect machines to queues
        for (QueueDTO queueDTO : canvasData.getQueues()) {
            Queue queue = queues.get(queueDTO.getId());
            for (String machineId : queueDTO.getMachineIds()) {
                Machine machine = machines.get(machineId);
                queue.connectMachine(machine);
            }
        }

        // Start the simulation
        for (Queue queue : queues.values()) {
            // Add processes to the queue
            int n = queue.getProcessCount();
            for (int i = 0; i < n; i++) {
                queue.addProcess(new Process());
            }
        }
    }

    public void stopSimulation() {
        // Save the current state before stopping
        simulationMemento.setCanvas(canvasData);

        // Stop all machines
        for (Machine machine : machines.values()) {
            machine.stop();
        }

        // Clear all queues
        for (Queue queue : queues.values()) {
            queue.clearProcesses();
        }
        System.out.println("Simulation stopped.");
    }

    public void replaySimulation() {
        if (simulationMemento.getCanvas() != null) {
            // Restore the simulation state
            initializeSimulation(simulationMemento.getCanvas());
            System.out.println("Simulation replayed from the saved state.");
        } else {
            System.out.println("No saved state found to replay.");
        }
    }
}
