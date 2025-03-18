package org.example.gstmeetapi.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final MeetingWebSocketHandler meetingWebSocketHandler;

    public WebSocketConfig(MeetingWebSocketHandler meetingWebSocketHandler) {
        this.meetingWebSocketHandler = meetingWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(meetingWebSocketHandler, "/diemdanh/{id}")
                .setAllowedOrigins("*"); // Cho phép kết nối từ bất kỳ client nào
    }
}
