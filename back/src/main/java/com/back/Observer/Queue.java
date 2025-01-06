package com.back.Observer;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.List;

public class Queue implements Observer {
    private final String id;
    private final BlockingQueue<Process> processes = new LinkedBlockingQueue<>();
    private final List<Machine> connectedMachines = new ArrayList<>();
    private final Integer processCount;

    public Queue(String id, Integer processCount) {
        this.processCount = processCount;
        this.id = id;
    }

    public Integer getProcessCount() {
        return processCount;
    }

    public String getId() {
        return id;
    }

    public List<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

    public void addProcess(Process process) {
        processes.add(process);
        assignProcessToMachine();
    }

    public void connectMachine(Machine machine) {
        connectedMachines.add(machine);
        machine.addObserver(this); // Observe the machine for availability
    }

    private void assignProcessToMachine() {
        for (Machine machine : connectedMachines) {
            if (machine.isIdle()) {
                try {
                    Process process = processes.take(); // Take the next process
                    machine.assignProcess(process);
                    break; // Assign to the first available machine
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Queue " + id + " was interrupted.");
                }
            }
        }
    }

    @Override
    public void update(String message) {
        if (message.equals("Machine is idle")) {
            assignProcessToMachine(); // Assign a process if the machine is idle
        }
    }
}
