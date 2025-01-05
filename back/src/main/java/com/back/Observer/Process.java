package com.back.Observer;
import java.awt.Color;
import java.util.UUID;

public class Process {
    private final UUID id;
    private final Color color;

    public Process() {
        this.id = UUID.randomUUID();
        this.color = new Color(
                (int) (Math.random() * 255),
                (int) (Math.random() * 255),
                (int) (Math.random() * 255)
        );
    }

    public UUID getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }
}
