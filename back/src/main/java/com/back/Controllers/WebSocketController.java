package com.back.Controllers;

import com.back.DTO.CanvasData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class WebSocketController extends TextWebSocketHandler {

    private final SimulationService simulationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = new HashSet<>(); // Track all connected sessions

    @Autowired
    public WebSocketController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // Handle incoming WebSocket messages
        String payload = message.getPayload();
        System.out.println("!!!!! MESSAGE RECEIVED: " + payload);

        try {
            // Parse the JSON payload into a CanvasData object
            CanvasData canvasData = objectMapper.readValue(payload, CanvasData.class);

            // Initialize the simulation using the SimulationService
            simulationService.initializeSimulation(canvasData);

            // Broadcast the updated canvas data to all connected clients
            broadcastCanvasData(canvasData);
        } catch (Exception e) {
            // Handle errors (e.g., invalid JSON payload)
            System.err.println("Error processing WebSocket message: " + e.getMessage());
            session.sendMessage(new TextMessage("Error: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Handle new WebSocket connection
        System.out.println("!!!!! NEW CONNECTION ESTABLISHED: " + session.getId());
        sessions.add(session); // Add the session to the set of connected sessions
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        // Handle WebSocket connection closure
        System.out.println("!!!!! CONNECTION CLOSED: " + session.getId());
        sessions.remove(session); // Remove the session from the set of connected sessions
    }

    // Broadcast canvas data to all connected clients
    private void broadcastCanvasData(CanvasData canvasData) throws IOException {
        String jsonPayload = objectMapper.writeValueAsString(canvasData);
        TextMessage message = new TextMessage(jsonPayload);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        }
    }
}