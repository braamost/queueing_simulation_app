package com.back.DTO;

import java.awt.Color;

public class ColorDTO {
    private int red;
    private int green;
    private int blue;

    public ColorDTO() {}

    public ColorDTO(Color color) {
        if (color != null) {
            this.red = color.getRed();
            this.green = color.getGreen();
            this.blue = color.getBlue();
        }
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }
}