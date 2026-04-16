package com.metrics.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecognitionServiceException.class)
    public ResponseEntity<Map<String, Object>> handleRecognitionServiceUnavailable(RecognitionServiceException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "DOWN",
                "service", "diagram-recognition",
                "message", ex.getMessage()
            ));
    }
}
