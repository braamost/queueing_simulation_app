package com.back.snapShot;
import com.back.DTO.CanvasData;
import com.back.Observer.Machine;
import com.back.Observer.Queue;
import com.back.Observer.Process;

import java.awt.*;
import java.util.List;

public class SimulationMemento {
//    private final List<Process> processes;
//    private final List<Machine> machines;
//    private final List<Queue> queues;
    private CanvasData canvas;

//    public SimulationMemento(List<Process> processes, List<Machine> machines, List<Queue> queues) {
//        this.processes = processes;
//        this.machines = machines;
//        this.queues = queues;
//    }

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

    public CanvasData getCanvas() {
        return canvas;
    }

    public void setCanvas(CanvasData canvas) {
        this.canvas = canvas;
    }
}
