package org.example.gstmeetapi.configuration;

import org.example.gstmeetapi.api.Api;
import org.example.gstmeetapi.api.dto.ConferenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private ConferenceManager conferenceManager;

    @Autowired
    private Api api;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        stompEndpointRegistry
                .addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:4200",
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "https://localhost:8080",
                        "https://sec.cmcati.vn",
                        "https://meet.cmcati.vn/",
                        "https://cmeet.cmcati.vn/",
                        "http://10.1.6.53:4203",
                        "http://10.1.6.53:4204",
                        "https://meet-dev.cmcati.vn",
                        "http://10.1.6.11:8085",
                        "https://10.1.6.11:8085"
                )
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(819200);
        registration.setSendBufferSizeLimit(819200);
        registration.setSendTimeLimit(2048 * 2048);
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(819200);
        container.setMaxBinaryMessageBufferSize(819200);
        return container;
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        String roomId = conferenceManager.getRoomId(sessionId);
        if(roomId != null) {
            api.stopWhip(roomId);
            conferenceManager.removeConference(sessionId);
        }
    }
}
