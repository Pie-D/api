package org.example.gstmeetapi.helper.response.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseType {
    public int code ;
    public String message;
    public ResponseType(int code , String message){
        this.code = code;
        this.message = message;
    }
}