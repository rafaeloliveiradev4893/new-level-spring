package com.newlevel.new_level_spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResponsiveStatusException extends ResponseStatusException {
  public ResponsiveStatusException(String message) {
    this(HttpStatus.NOT_FOUND, message);
  }

  public ResponsiveStatusException(HttpStatus status, String message) {
    super(status, message);
  }
}