package com.back.Observer;
import java.awt.Color;
import java.util.UUID;

public class Machine extends Observable implements Runnable {
    private final String id;
    private final Queue nextQueue;
    private Process currentProcess;
    private volatile boolean isIdle = true;
    private Color color;
    private int runningTime;

    public Machine(String id, Queue nextQueue) {
        this.id = id;
        this.nextQueue = nextQueue;
    }

    public boolean isIdle() {
        return isIdle;
    }

    public Color getColor() {
        return color;
    }

    public String getId() {
        return id;
    }
    public void stop() {
        isIdle = false; // Stop the machine
        Thread.currentThread().interrupt();
    }
    public void assignProcess(Process process) {
        this.currentProcess = process;
        this.isIdle = false;
        this.color = process.getColor();
        this.runningTime = (int) (Math.random() * 5000) + 1000;
        new Thread(this).start(); // Start processing
    }

    @Override
    public void run() {
        try {
            System.out.println(id + " processing " + currentProcess.getId());
            Thread.sleep(runningTime); // Simulate processing time

            // Send the processed task to the next queue
            if(nextQueue != null) nextQueue.addProcess(currentProcess);
            else System.out.println(currentProcess.getId() + " dropped");
            System.out.println(id + " finished processing " + currentProcess.getId());

            // Notify observers that the machine is idle
            isIdle = true;
            notifyObservers("Machine is idle");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(id + " was interrupted.");
        }
    }

}
