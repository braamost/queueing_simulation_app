package com.back.Observer;

import java.awt.Color;

public class Process {
    private static Integer idCounter = 0;
    private final Integer id;
    private final Color color;

    public Process() {
        this.id = idCounter++;
        this.color = new Color(
                (int) (Math.random() * 255),
                (int) (Math.random() * 255),
                (int) (Math.random() * 255)
        );
    }

    public Integer getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }
}