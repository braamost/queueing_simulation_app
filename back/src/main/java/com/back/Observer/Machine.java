package com.back.Observer;

import com.back.Controllers.SimulationService;
import com.back.DTO.ColorDTO;
import com.back.DTO.SimulationStateDTO;
import java.awt.Color;

public class Machine extends Observable implements Runnable {
    private final String id;
    private final Queue nextQueue;
    private Process currentProcess;
    private volatile boolean isIdle = true;
    private Color color;
    private int runningTime;
    private final SimulationService simulationService;

    public Machine(String id, Queue nextQueue, SimulationService simulationService) {
        this.id = id;
        this.nextQueue = nextQueue;
        this.simulationService = simulationService;
    }

    public boolean isIdle() {
        return isIdle;
    }

    public String getId() {
        return id;
    }

    public void stop() {
        isIdle = false;
        Thread.currentThread().interrupt();
    }

    public void assignProcess(Process process) {
        this.currentProcess = process;
        this.isIdle = false;
        this.color = process.getColor();
        this.runningTime = (int) (Math.random() * 5000) + 1000;

        SimulationStateDTO.MachineStateDTO state = new SimulationStateDTO.MachineStateDTO(
                id,
                false,
                new ColorDTO(color),
                runningTime,
                process.getId().toString()
        );

        simulationService.updateMachineState(state);
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(runningTime);

            if (nextQueue != null) {
                System.out.println(nextQueue.getId());
                nextQueue.addProcess(currentProcess);
                nextQueue.assignProcessesToMachines();
            }
            System.out.println("Machine " + id + " is done with process " + currentProcess.getId());
            isIdle = true;

            SimulationStateDTO.MachineStateDTO state = new SimulationStateDTO.MachineStateDTO(
                    id,
                    true,
                    null,
                    0,
                    null
            );
            simulationService.updateMachineState(state);
            notifyObservers(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}