package com.back.snapShot;

import com.back.Observer.Machine;
import com.back.Observer.Queue;
import com.back.Observer.Process;

import java.util.ArrayList;
import java.util.List;

public class SimulationCareTaker {
    private final List<SimulationMemento> mementos = new ArrayList<>();

    public void saveSnapshot(SimulationMemento memento) {
        mementos.add(memento);
    }

    public SimulationMemento getSnapshot(int index) {
        return mementos.get(index);
    }

    public void replaySimulation() {
        for (SimulationMemento memento : mementos) {
            System.out.println("Replaying snapshot...");
            // Replay the snapshot (e.g., print or reconstruct the state)
            for (Process process : memento.getProcesses()) {
                System.out.println("Process: " + process.getId() + ", Color: " + process.getColor());
            }
            for (Machine machine : memento.getMachines()) {
                System.out.println("Machine: " + machine.getId() + ", Idle: " + machine.isIdle());
            }
            for (Queue queue : memento.getQueues()) {
                System.out.println("Queue: " + queue.getId() + ", Processes: " + queue.getProcesses().size());
            }
            System.out.println("-----------------------------");
        }
    }
}
