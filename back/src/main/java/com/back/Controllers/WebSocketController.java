package com.back.Controllers;

import com.back.DTO.CanvasData;
import com.back.DTO.SimulationStateDTO;
import com.back.Configuration.SimulationStateEvent;
import com.back.Observer.Process;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketController extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    private final SimulationService simulationService;

    @Autowired
    public WebSocketController(ObjectMapper objectMapper, SimulationService simulationService) {
        this.objectMapper = objectMapper;
        this.simulationService = simulationService;
    }

    @EventListener
    public void handleSimulationStateEvent(SimulationStateEvent event) {
        broadcastState(event.getState());
    }

    public void broadcastState(SimulationStateDTO state) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(state);
            TextMessage message = new TextMessage(jsonPayload);

            sessions.removeIf(session -> {
                if (session != null && session.isOpen()) {
                    try {
                        session.sendMessage(message);
                        return false; // Keep the session in the set
                    } catch (IOException e) {
                        logger.error("Error sending message to client {}: {}", session.getId(), e.getMessage());
                        try {
                            session.close(CloseStatus.SERVER_ERROR);
                        } catch (IOException ex) {
                            logger.error("Error closing WebSocket session: {}", ex.getMessage());
                        }
                        return true; // Remove the session from the set
                    }
                } else {
                    return true; // Remove the session from the set
                }
            });
        } catch (IOException e) {
            logger.error("Error serializing state to JSON: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New WebSocket connection established: {}", session.getId());
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket connection closed: {}", session.getId());
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket transport error: {}", exception.getMessage());
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            logger.error("Error closing WebSocket session: {}", e.getMessage());
        }
        sessions.remove(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            logger.debug("Received WebSocket message: {}", payload);
            System.out.println("Received WebSocket message: " + payload);

            JsonNode jsonNode = objectMapper.readTree(payload);

            if (jsonNode.has("type")) {
                String type = jsonNode.get("type").asText();

                switch (type) {
                    case "INIT_SIMULATION":
                        handleInitSimulation(jsonNode);
                        break;
                    case "UPDATE_PROCESS_COUNT":
                        handleUpdateProcessCount(jsonNode);
                        break;
                    case "STOP_SIMULATION":
                        handleStopSimulation();
                        break;
                    case "PAUSE_SIMULATION":
                        pauseSimulation();
                        break;
                    case "RESUME_SIMULATION":
                        resumeSimulation();
                        break;
                    case "REPLAY_SIMULATION":
                        handleReplaySimulation();
                        break;
                    case "END_REPLAY":
                        handleEndReplay();
                    default:
                        logger.warn("Unknown message type received: {}", type);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: {}", e.getMessage());
            try {
                session.sendMessage(new TextMessage("Error: " + e.getMessage()));
            } catch (IOException ex) {
                logger.error("Error sending error message to client: {}", ex.getMessage());
            }
        }
    }

    private void handleReplaySimulation() {
        simulationService.replaySimulation();
    }

    private void handleEndReplay() {
        simulationService.handleEndReplay();
    }

    private void handleInitSimulation(JsonNode jsonNode) throws Exception {
        // Parse the CanvasData from the message
        CanvasData canvasData = objectMapper.treeToValue(jsonNode.get("data"), CanvasData.class);

        // Initialize the simulation using the SimulationService
        simulationService.initializeSimulation(canvasData);

        // Log the initialization
        logger.info("Simulation initialized with data: {}", canvasData);
    }

    private void handleUpdateProcessCount(JsonNode jsonNode) {
        // Extract queueId and newCount from the message
        String queueId = jsonNode.get("queueId").asText();

        int newCount = jsonNode.get("count").asInt();

        // Update the process count using the SimulationService
        simulationService.updateQueueProcessCount(queueId, newCount);

        // Log the update
        logger.info("Updated process count for queue {} to {}", queueId, newCount);
    }

    private void handleStopSimulation() {
        // Stop the simulation using the SimulationService
        simulationService.stopSimulation();

        Process.setIdCounter(0);

        // Close all WebSocket sessions
        closeAllSessions();

        // Log the stop action
        logger.info("Simulation stopped and all WebSocket sessions closed");
    }

    private void pauseSimulation() {
        simulationService.pauseSimulation();
    }

    private void resumeSimulation() {
        simulationService.resumeSimulation();
    }

    private void closeAllSessions() {
        sessions.removeIf(session -> {
            if (session.isOpen()) {
                try {
                    session.close(CloseStatus.SERVICE_RESTARTED);
                } catch (IOException e) {
                    logger.error("Error closing WebSocket session {}: {}", session.getId(), e.getMessage());
                }
            }
            return true; // Remove the session from the set
        });
    }

}