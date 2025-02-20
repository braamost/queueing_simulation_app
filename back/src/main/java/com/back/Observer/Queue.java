package com.back.Observer;

import com.back.Controllers.SimulationService;
import com.back.DTO.SimulationStateDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Queue implements Observer {
    private final String id;
    private final BlockingQueue<Process> processes = new LinkedBlockingQueue<>();
    private final Map<String, Machine> connectedMachines = new HashMap<>();
    private final SimulationService simulationService;
    private Integer processCount;

    public Queue(String id, Integer processCount, SimulationService simulationService) {
        this.id = id;
        this.processCount = processCount;
        this.simulationService = simulationService ;
    }

    public void addProcess(Process process) {
        processes.add(process);
        processCount++;
    }

    public Process removeProcess() {
        if(processes.isEmpty()) return null;
        Process process = processes.poll();
        processCount--;
        return process;
    }

    public synchronized void notifyServer() {
        // update new process count in simulationService
        SimulationStateDTO.QueueStateDTO state = new SimulationStateDTO.QueueStateDTO(id, processCount);
        simulationService.updateQueueState(state);
    }

    public synchronized void clearProcesses() {
        processes.clear();
        processCount = 0;
        notifyServer();
    }

    public void updateProcessCount(int newCount) {
        for (int i = 0; i < newCount ; i++) {
            addProcess(new Process());
        }
        notifyServer();
        assignProcessesToMachines();
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public String getId() {
        return id;
    }

    public void connectMachine(Machine machine) {
        connectedMachines.put(machine.getId(), machine);
        machine.addObserver(this);
    }

    public void assignProcessesToMachines() {
        for(int i = 0; i < processCount; i++) {
            for (Machine machine : connectedMachines.values()) {
                if(machine.isIdle()) {
                    Process process = removeProcess();
                    if(process != null) machine.assignProcess(process);
                    break;
                }
            }
        }
        notifyServer();
    }

    @Override
    public synchronized void update(String message, String machineId) {
        if (message.equals("Machine is idle")) {
            if(!processes.isEmpty()) {
                Machine machine = connectedMachines.get(machineId);
                Process process = removeProcess();
                if(process != null) machine.assignProcess(process);
            }
            notifyServer();
        }
    }

    public void setProcessCount(int processCount) { this.processCount = processCount; }
}