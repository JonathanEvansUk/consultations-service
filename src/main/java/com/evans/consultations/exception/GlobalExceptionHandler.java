package com.evans.consultations.exception;

import com.evans.consultations.model.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception e) {
        log.error("Unexpected exception: ", e);

        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
    }

    @ExceptionHandler(ConsultationsException.class)
    public ResponseEntity<ErrorDto> handleConsultationsException(ConsultationsException e) {
        log.error("Consultations exception: {}", e.getMessage());

        return createErrorResponse(e.getStatus(), e.getMessage());
    }

    private ResponseEntity<ErrorDto> createErrorResponse(HttpStatus status, String message) {
        ErrorDto errorDto = new ErrorDto().message(message);
        return ResponseEntity.status(status).body(errorDto);
    }
}
