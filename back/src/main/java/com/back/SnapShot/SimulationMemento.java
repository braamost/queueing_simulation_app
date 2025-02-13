package com.back.SnapShot;

import com.back.DTO.SimulationStateDTO;

public class SimulationMemento {
    private final SimulationStateDTO state;

    public SimulationMemento(SimulationStateDTO state) {
        this.state = state;
    }

    public SimulationStateDTO getState() {
        return state;
    }
}
