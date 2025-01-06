package com.back.DTO;

import java.util.List;

public class CanvasData {
    private List<QueueDTO> queues;
    private List<MachineDTO> machines;

    // Default constructor needed for JSON deserialization
    public CanvasData() {}

    public CanvasData(List<QueueDTO> queues, List<MachineDTO> machines) {
        this.queues = queues;
        this.machines = machines;
    }

    public List<QueueDTO> getQueues() {
        return queues;
    }

    public void setQueues(List<QueueDTO> queues) {
        this.queues = queues;
    }

    public List<MachineDTO> getMachines() {
        return machines;
    }

    public void setMachines(List<MachineDTO> machines) {
        this.machines = machines;
    }

    @Override
    public String toString() {
        return "CanvasData{" +
                "queues=" + queues +
                ", machines=" + machines +
                '}';
    }
}