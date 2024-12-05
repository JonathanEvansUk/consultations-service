package com.evans.consultations.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConsultationsException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public ConsultationsException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}
