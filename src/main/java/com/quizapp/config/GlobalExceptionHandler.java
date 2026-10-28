package com.quizapp.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Turns any error into a clean JSON message the frontend can show. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handle(Exception e) {
        String msg = e.getMessage() == null ? "Something went wrong" : e.getMessage();
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }
}
