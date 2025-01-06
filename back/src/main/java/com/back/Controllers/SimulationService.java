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

@Service
public class SimulationService {
    private final Map<String, Queue> queues = new HashMap<>();
    private final Map<String, Machine> machines = new HashMap<>();

    public void initializeSimulation(CanvasData canvasData) {
        // Create queues
        for (QueueDTO queueDTO : canvasData.getQueues()) {
            Queue queue = new Queue(queueDTO.getId(), queueDTO.getNProcesses());
            queues.put(queueDTO.getId(), queue);
        }

        // Create machines
        for (MachineDTO machineDTO : canvasData.getMachines()) {
            Queue nextQueue = null;
            if(!"".equals(machineDTO.getQueueId()))
                nextQueue = queues.get(machineDTO.getQueueId());
            Machine machine = new Machine(machineDTO.getId(), nextQueue);
            machines.put(machineDTO.getId(), machine);
        }

        for (QueueDTO queueDTO : canvasData.getQueues()) {
            Queue queue = queues.get(queueDTO.getId());
            for(String machineId : queueDTO.getMachineIds()) {
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
}
