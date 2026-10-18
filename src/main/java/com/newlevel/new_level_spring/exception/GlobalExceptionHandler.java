package com.newlevel.new_level_spring.exception;

import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<Map<String, String>> handleConflict() {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(Map.of("message", "A operação conflitou com outra requisição. Tente novamente."));
  }
}