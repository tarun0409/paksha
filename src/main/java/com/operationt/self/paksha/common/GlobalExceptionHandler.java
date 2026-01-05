package com.operationt.self.paksha.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ IllegalArgumentException.class, NoSuchElementException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequest(RuntimeException e) {
//        System.out.println(">>> GlobalExceptionHandler hit: " + e.getClass().getName());
        return Map.of("error", (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName());
    }
}

