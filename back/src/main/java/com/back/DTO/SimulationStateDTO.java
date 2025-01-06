package com.back.DTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationStateDTO {
    private Map<String, MachineStateDTO> machineStates = new HashMap<>();

    public Map<String, MachineStateDTO> getMachineStates() {
        return machineStates;
    }

    public void setMachineStates(Map<String, MachineStateDTO> machineStates) {
        this.machineStates = machineStates;
    }

    public static class MachineStateDTO {
        private String id;
        private boolean idle;
        private ColorDTO color;
        private int runningTime;
        private String currentProcessId;

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
}