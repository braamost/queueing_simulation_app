package com.back.DTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationStateDTO {
    private Map<String, MachineStateDTO> machineStates = new HashMap<>();
    private Map<String, QueueStateDTO> queueStates = new HashMap<>();

    public Map<String, MachineStateDTO> getMachineStates() {
        return machineStates;
    }
    public Map<String, QueueStateDTO> getQueueStates() {
        return queueStates;
    }

    public void setMachineStates(Map<String, MachineStateDTO> machineStates) {
        this.machineStates = machineStates;
    }

    public void setQueueStates(Map<String, QueueStateDTO> queueStates) {
        this.queueStates = queueStates;
    }

    public static class MachineStateDTO {
        private String id;
        private boolean idle;
        private ColorDTO color;
        private int runningTime;
        private String currentProcessId;

        public MachineStateDTO(String id, boolean idle, ColorDTO color, int runningTime, String currentProcessId) {
            this.id = id;
            this.idle = idle;
            this.color = color;
            this.runningTime = runningTime;
            this.currentProcessId = currentProcessId;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public boolean isIdle() { return idle; }
        public void setIdle(boolean idle) { this.idle = idle; }
        public ColorDTO getColor() { return color; }
        public void setColor(ColorDTO color) { this.color = color; }
        public int getRunningTime() { return runningTime; }
        public void setRunningTime(int runningTime) { this.runningTime = runningTime; }
        public String getCurrentProcessId() { return currentProcessId; }
        public void setCurrentProcessId(String currentProcessId) { this.currentProcessId = currentProcessId; }
    }

    public static class QueueStateDTO {
        private String id;
        private int processCount;
        // add other properties as needed

        public QueueStateDTO(String id, int processCount) {
            this.id = id;
            this.processCount = processCount;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getProcessCount() { return processCount; }
        public void setProcessCount(int processCount) { this.processCount = processCount; }
    }
}