package com.back.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QueueDTO {
    @JsonProperty("id")
    private String id;
    @JsonProperty("nProcesses")
    private Integer nProcesses;
    @JsonProperty("machineIds")
    private List<String> machineIds;

    // Default constructor needed for JSON deserialization
    public QueueDTO() {}

    public QueueDTO(String id, Integer nProcesses, List<String> machineIds) {
        this.id = id;
        this.nProcesses = nProcesses;
        this.machineIds = machineIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNProcesses() {
        return nProcesses;
    }

    public void setNProcesses(Integer nProcesses) {
        this.nProcesses = nProcesses;
    }

    public List<String> getMachineIds() {
        return machineIds;
    }

    public void setMachineIds(List<String> machineIds) {
        this.machineIds = machineIds;
    }

    @Override
    public String toString() {
        return "QueueDTO{" +
                "id='" + id + '\'' +
                ", nProcesses=" + nProcesses +
                ", machineIds=" + machineIds +
                '}';
    }
}