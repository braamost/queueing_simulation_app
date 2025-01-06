package com.back.Configuration;

import com.back.Controllers.WebSocketController;
import com.back.Controllers.SimulationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SimulationService simulationService;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebSocketConfig(SimulationService simulationService, ObjectMapper objectMapper) {
        this.simulationService = simulationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketController(), "/simulation")
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketController webSocketController() {
        return new WebSocketController(objectMapper, simulationService);
    }
}