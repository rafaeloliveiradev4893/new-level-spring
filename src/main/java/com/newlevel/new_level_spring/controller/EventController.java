package com.newlevel.new_level_spring.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newlevel.new_level_spring.model.DTOS.EventRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.EventResponseDTO;
import com.newlevel.new_level_spring.model.DTOS.EventUpdateDTO;
import com.newlevel.new_level_spring.services.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/event")
@RestController
public class EventController {

  private final EventService service;

  @GetMapping
  public List<EventResponseDTO> getEvents(@AuthenticationPrincipal Jwt jwt) {
    return service.listEventsByUser(jwt);
  }

  // Ex.: GET /api/event/period?start=2026-10-01T00:00:00&end=2026-10-31T23:59:59
  @GetMapping("/period")
  public List<EventResponseDTO> getEventsByPeriod(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
      @AuthenticationPrincipal Jwt jwt) {
    return service.listEventsByPeriod(jwt, start, end);
  }

  @GetMapping("/{eventId}")
  public EventResponseDTO getEventById(@PathVariable Long eventId, @AuthenticationPrincipal Jwt jwt) {
    return service.getEventById(eventId, jwt);
  }

  @PostMapping
  public EventResponseDTO addEvent(@Valid @RequestBody EventRequestDTO eventDTO, @AuthenticationPrincipal Jwt jwt) {
    return service.createEvent(jwt, eventDTO);
  }

  @PutMapping("/{eventId}")
  public EventResponseDTO updateEvent(@PathVariable Long eventId, @Valid @RequestBody EventRequestDTO eventDTO, @AuthenticationPrincipal Jwt jwt) {
    return service.updateEvent(eventId, jwt, eventDTO);
  }

  @PatchMapping("/{eventId}")
  public EventResponseDTO updatePartialEvent(@PathVariable Long eventId, @Valid @RequestBody EventUpdateDTO eventDTO, @AuthenticationPrincipal Jwt jwt) {
    return service.updatePartialEvent(eventId, jwt, eventDTO);
  }

  @PatchMapping("/{eventId}/done")
  public EventResponseDTO completeEvent(@PathVariable Long eventId, @AuthenticationPrincipal Jwt jwt) {
    return service.completeEvent(eventId, jwt);
  }

  @DeleteMapping("/{eventId}")
  public void deleteEvent(@PathVariable Long eventId, @AuthenticationPrincipal Jwt jwt) {
    service.deleteEvent(eventId, jwt);
  }
}