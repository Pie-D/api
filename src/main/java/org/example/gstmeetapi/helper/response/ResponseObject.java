package org.example.gstmeetapi.helper.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.gstmeetapi.helper.response.entity.ResponseType;

import java.io.Serializable;

@Getter
@Setter
@Data
public class ResponseObject <T> implements Serializable {
    private String message;
    private Integer code;
    private T data;
    public ResponseObject(ResponseType responseType, T data) {
        this.code = responseType.getCode();
        this.message = responseType.getMessage();
        this.data = data;
    }
    public ResponseObject(ResponseType responseType) {
        this.code = responseType.getCode();
        this.message = responseType.getMessage();
    }

    @JsonCreator
    public ResponseObject(@JsonProperty("code") Integer code, @JsonProperty("message") String message, @JsonProperty("data") T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}