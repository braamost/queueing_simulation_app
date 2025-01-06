package com.back.DTO;

public class ProcessDTO {
    private Integer id;
    private ColorDTO color;

    public ProcessDTO() {}

    public ProcessDTO(Integer id, ColorDTO color) {
        this.id = id;
        this.color = color;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ColorDTO getColor() {
        return color;
    }

    public void setColor(ColorDTO color) {
        this.color = color;
    }
}