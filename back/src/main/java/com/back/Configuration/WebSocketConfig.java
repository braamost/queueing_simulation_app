package com.back.Configuration;

import com.back.Controllers.WebSocketController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketController webSocketController;

    public WebSocketConfig(WebSocketController webSocketController) {
        this.webSocketController = webSocketController;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register the WebSocket handler and specify the endpoint
        registry.addHandler((WebSocketHandler) webSocketController, "/ws")
                .setAllowedOrigins("*"); // Allow all origins (adjust as needed)
    }
}