package com.back.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MachineDTO {
    @JsonProperty("id")
    private String id;
    @JsonProperty("queueId")
    private String queueId; // Changed from QueueId to match JSON

    // Default constructor needed for JSON deserialization
    public MachineDTO() {}

    public MachineDTO(String id, String queueId) {
        this.id = id;
        this.queueId = queueId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    @Override
    public String toString() {
        return "MachineDTO{" +
                "id='" + id + '\'' +
                ", queueId='" + queueId + '\'' +
                '}';
    }
}