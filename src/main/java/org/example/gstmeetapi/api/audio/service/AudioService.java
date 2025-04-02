package org.example.gstmeetapi.api.audio.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gstmeetapi.api.audio.dto.AudioWhip;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AudioService {
    public String audioConnectWhip(AudioWhip audioWhip) {

        return "Connection Audio";
    }
}
