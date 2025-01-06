package com.back.Configuration;

import com.back.DTO.SimulationStateDTO;
import org.springframework.context.ApplicationEvent;

public class SimulationStateEvent extends ApplicationEvent {
    private final SimulationStateDTO state;

    public SimulationStateEvent(Object source, SimulationStateDTO state) {
        super(source);
        this.state = state;
    }

    public SimulationStateDTO getState() {
        return state;
    }
}
