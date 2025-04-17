package org.example.gstmeetapi.configuration;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class MeetingWebSocketHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, Set<WebSocketSession>> meetingSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String meetingId = getMeetingId(session);
        meetingSessions.computeIfAbsent(meetingId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String meetingId = getMeetingId(session);
        Set<WebSocketSession> sessions = meetingSessions.get(meetingId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                meetingSessions.remove(meetingId);
            }
        }
    }

    public void sendImageToMeeting(String meetingId, byte[] imageData) {
        Set<WebSocketSession> sessions = meetingSessions.get(meetingId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                try {
                    session.sendMessage(new BinaryMessage(imageData));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getMeetingId(WebSocketSession session) {
        return session.getUri().getPath().split("/")[2]; // Lấy ID phòng họp từ URL
    }
}