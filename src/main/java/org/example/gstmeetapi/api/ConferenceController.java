package org.example.gstmeetapi.api;

import lombok.extern.slf4j.Slf4j;
import org.example.gstmeetapi.api.dto.ConferenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ConferenceController {
    @Autowired
    private ConferenceManager conferenceManager;

    @MessageMapping("/conference/{roomId}")
    public void socketSpeechToTextEdit(@DestinationVariable("roomId") String roomId,
                                       SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        conferenceManager.addConference(sessionId, roomId);
    }
}
