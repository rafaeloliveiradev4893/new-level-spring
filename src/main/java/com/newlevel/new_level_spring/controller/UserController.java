package com.newlevel.new_level_spring.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newlevel.new_level_spring.model.DTOS.UserResponseDTO;
import com.newlevel.new_level_spring.services.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

  private final UserService service;

  @GetMapping("/me")
  public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(service.getCurrentUser(jwt));
  }

  @PostMapping
  public ResponseEntity<UserResponseDTO> createUser(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity
      .status(201)
      .body(service.createUser(jwt));
  }

  @PatchMapping 
  public ResponseEntity<UserResponseDTO> updateUser(@AuthenticationPrincipal Jwt jwt) {
    return ResponseEntity.ok(service.updateUser(jwt));
  }

  @DeleteMapping
  public void deleteUser(@AuthenticationPrincipal Jwt jwt) {
    service.deleteUser(jwt);
  }
}
