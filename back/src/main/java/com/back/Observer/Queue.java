package com.back.Observer;

import com.back.Controllers.SimulationService;
import com.back.DTO.ProcessDTO;
import com.back.DTO.ColorDTO;
import com.back.DTO.SimulationStateDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Queue implements Observer {
    private final String id;
    private final BlockingQueue<Process> processes = new LinkedBlockingQueue<>();
    private final List<Machine> connectedMachines = new ArrayList<>();
    private final Integer processCount;

    public Queue(String id, Integer processCount) {
        this.id = id;
        this.processCount = processCount;
    }

    public void addProcess(Process process) {
        processes.add(process);
        assignProcessToMachine();
    }

    public void updateProcessCount(int newCount) {
        int diff = newCount - processes.size();
        if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                addProcess(new Process());
            }
        } else if (diff < 0) {
            for (int i = 0; i < Math.abs(diff) && !processes.isEmpty(); i++) {
                processes.poll();
            }
        }
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public String getId() {
        return id;
    }

    public void connectMachine(Machine machine) {
        connectedMachines.add(machine);
        machine.addObserver(this);
    }

    private void assignProcessToMachine() {
        for (Machine machine : connectedMachines) {
            if (machine.isIdle() && !processes.isEmpty()) {
                try {
                    Process process = processes.take();
                    machine.assignProcess(process);
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void update(String message) {
        if (message.equals("Machine is idle")) {
            assignProcessToMachine();
        }
    }

    public void clearProcesses() {
        processes.clear();
    }
}