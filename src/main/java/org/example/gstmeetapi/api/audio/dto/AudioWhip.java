package org.example.gstmeetapi.api.audio.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AudioWhip {
   int durationSeconds;
   String roomId;
   String domain;
   String whipEndpoint;
}
