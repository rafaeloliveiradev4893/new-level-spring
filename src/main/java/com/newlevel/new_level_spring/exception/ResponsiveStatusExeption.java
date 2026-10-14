package com.newlevel.new_level_spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResponsiveStatusExeption extends ResponseStatusException {
  public ResponsiveStatusExeption(String message) {
    this(HttpStatus.NOT_FOUND, message);
  }

  public ResponsiveStatusExeption(HttpStatus status, String message) {
    super(status, message);
  }
}