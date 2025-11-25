package org.example.gstmeetapi.api.dto;

import lombok.Data;

@Data
public class WhipConnectDto {
    private String roomId;
    private String domain;
    private String whipEndpoint;
    private String xmppDomain;
    private String nickname = "CMEET-BOT";
    private Boolean isRecord = false;
}
