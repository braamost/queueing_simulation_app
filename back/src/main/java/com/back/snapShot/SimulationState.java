//package com.back.snapShot;
//
//import com.back.Observer.Machine;
//import com.back.Observer.Queue;
//import com.back.Observer.Process;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SimulationState {
//    private List<Process> processes;
//    private List<Machine> machines;
//    private List<Queue> queues;
//
//    public SimulationState(List<Process> processes, List<Machine> machines, List<Queue> queues) {
//        this.processes = processes;
//        this.machines = machines;
//        this.queues = queues;
//    }
//
//    public SimulationMemento save() {
//        // Create and return a memento with the current state
//        return new SimulationMemento(new ArrayList<>(processes), new ArrayList<>(machines), new ArrayList<>(queues));
//    }
//
//    public void restore(SimulationMemento memento) {
//        // Restore the state from the memento
//        this.processes = memento.getProcesses();
//        this.machines = memento.getMachines();
//        this.queues = memento.getQueues();
//    }
//
//    public List<Process> getProcesses() {
//        return processes;
//    }
//
//    public List<Machine> getMachines() {
//        return machines;
//    }
//
//    public List<Queue> getQueues() {
//        return queues;
//    }
//}
