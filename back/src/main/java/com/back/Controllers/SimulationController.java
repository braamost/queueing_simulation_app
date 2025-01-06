package com.back.Controllers;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {
    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/stop")
    public void stopSimulation() {
        simulationService.stopSimulation();
    }

    @PostMapping("/replay")
    public void replaySimulation() {
        simulationService.replaySimulation();
    }
}