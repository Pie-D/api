package org.example.gstmeetapi.api.dto;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ConferenceManager {
    // sessionId conference và roomId
    private static final ConcurrentMap<String, String> conferenceMap = new ConcurrentHashMap<>();

    public String getRoomId(String sessionId) {
        return conferenceMap.get(sessionId);
    }

    public void addConference(String sessionId, String roomId) {
        conferenceMap.put(sessionId, roomId);
    }

    public void removeConference(String sessionId) {
        conferenceMap.remove(sessionId);
    }
}
