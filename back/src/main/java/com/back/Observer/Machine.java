package com.back.Observer;

import com.back.Controllers.SimulationService;
import com.back.DTO.ColorDTO;
import com.back.DTO.SimulationStateDTO;
import com.back.Singleton.PausingMechanism;

import java.awt.Color;

public class Machine extends Observable implements Runnable {
    private final String id;
    private final Queue nextQueue;
    private Process currentProcess;
    private volatile boolean isIdle = true;
    private Color color;
    private int runningTime;
    private final SimulationService simulationService;
    private final PausingMechanism pausingMechanism = PausingMechanism.getInstance();
    private volatile boolean running = true;

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

    private synchronized void IdleMachine() {
        this.isIdle = true;
        this.color = null;
        this.currentProcess = null;
        this.runningTime = 0;
        SimulationStateDTO.MachineStateDTO state = new SimulationStateDTO.MachineStateDTO(
                id,
                true,
                null,
                0,
                null
        );
        simulationService.updateMachineState(state);
    }

    public synchronized void stop() {
        this.running = false;

        IdleMachine();

        Thread.currentThread().interrupt();
    }

    public void assignProcess(Process process) {
        this.currentProcess = process;
        this.isIdle = false;
        this.color = process.getColor();
        this.runningTime = (int) (Math.random() * 8000) + 2000;

        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            pausingMechanism.checkPaused();
            if(!running) return;
            this.isIdle = false;
            SimulationStateDTO.MachineStateDTO state1 = new SimulationStateDTO.MachineStateDTO(
                    this.id,
                    false,
                    new ColorDTO(this.color),
                    this.runningTime,
                    this.currentProcess.getId().toString()
            );

            simulationService.updateMachineState(state1);

            // Simulate work with periodic pause checks
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < runningTime) {
                pausingMechanism.checkPaused(); // Check pause state periodically
                if(!running) return;
                Thread.sleep(100); // Sleep in small increments
            }

            pausingMechanism.checkPaused();
            if(!running) return;

            if (nextQueue != null) {
                nextQueue.addProcess(currentProcess);
                nextQueue.assignProcessesToMachines();
            }
            System.out.println("Machine " + id + " is done with process " + currentProcess.getId());

            IdleMachine();
            notifyObservers(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}