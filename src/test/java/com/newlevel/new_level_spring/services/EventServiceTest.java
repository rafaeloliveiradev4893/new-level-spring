package com.newlevel.new_level_spring.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

import com.newlevel.new_level_spring.exception.ResponsiveStatusException;
import com.newlevel.new_level_spring.model.DTOS.EventRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.EventResponseDTO;
import com.newlevel.new_level_spring.model.DTOS.EventUpdateDTO;
import com.newlevel.new_level_spring.model.Event;
import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.repository.EventRepository;
import com.newlevel.new_level_spring.repository.UserRepository;
import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

class EventServiceTest {

  private final EventRepository eventRepository = mock(EventRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final XpService xpService = mock(XpService.class);
  private final EventService eventService =
    new EventService(eventRepository, userRepository, xpService);
  private final Jwt jwt = Jwt.withTokenValue("token")
    .header("alg", "none")
    .subject("user-1")
    .build();

  private User user;
  private Event event;

  @BeforeEach
  void setUp() {
    user = User.builder().auth0Id("user-1").build();
    event = Event.builder()
      .id(1L)
      .user(user)
      .title("Event")
      .startDate(LocalDateTime.parse("2026-10-14T10:00:00"))
      .endDate(LocalDateTime.parse("2026-10-14T11:00:00"))
      .category(Category.STUDY)
      .difficulty(Difficulty.MEDIUM)
      .build();

    when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
    when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
  }

  @Test
  void createsEventForAuthenticatedUser() {
    when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
    when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
    EventRequestDTO request = EventRequestDTO.builder()
      .title("Study")
      .description("Review")
      .startDate(event.getStartDate())
      .endDate(event.getEndDate())
      .category(Category.STUDY)
      .difficulty(Difficulty.EASY)
      .build();

    EventResponseDTO response = eventService.createEvent(jwt, request);

    assertEquals("Study", response.getTitle());
    assertEquals(event.getStartDate(), response.getStartDate());
    verify(eventRepository).save(any(Event.class));
  }

  @Test
  void startsAnEventOnce() {
    EventResponseDTO response = eventService.startEvent(1L, jwt);

    assertNotNull(response.getActualStartAt());
    verify(eventRepository).save(event);
  }

  @Test
  void completesEventAndAwardsXp() {
    EventResponseDTO response = eventService.completeEvent(1L, jwt);

    assertTrue(response.getIsCompleted());
    assertNotNull(response.getCompletedAt());
    verify(xpService).addXpForTaskCompletion(user, Difficulty.MEDIUM, Category.STUDY);
    verify(userRepository).save(user);
    verify(eventRepository).save(event);
  }

  @Test
  void rejectsEventsOwnedByAnotherUser() {
    Jwt otherUserJwt = Jwt.withTokenValue("token")
      .header("alg", "none")
      .subject("user-2")
      .build();

    ResponsiveStatusException exception = assertThrows(
      ResponsiveStatusException.class,
      () -> eventService.getEventById(1L, otherUserJwt)
    );

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void rejectsInvalidEventDateRange() {
    ResponsiveStatusException exception = assertThrows(
      ResponsiveStatusException.class,
      () -> eventService.listEventsByPeriod(jwt, event.getEndDate(), event.getStartDate())
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
  }

  @Test
  void rejectsStartingAnAlreadyStartedEvent() {
    event.setActualStartAt(event.getStartDate());

    ResponsiveStatusException exception = assertThrows(
      ResponsiveStatusException.class,
      () -> eventService.startEvent(1L, jwt)
    );

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
  }

  @Test
  void appliesPartialUpdatesUsingExistingEventValuesForDateValidation() {
    EventUpdateDTO request = EventUpdateDTO.builder()
      .description("Updated description")
      .build();

    EventResponseDTO response = eventService.updatePartialEvent(1L, jwt, request);

    assertEquals("Event", response.getTitle());
    assertEquals("Updated description", response.getDescription());
    assertEquals(event.getStartDate(), response.getStartDate());
    assertEquals(event.getEndDate(), response.getEndDate());
    verify(eventRepository).save(event);
  }

  @Test
  void completesEventByTimeAndAwardsXpOnce() {
    EventResponseDTO response = eventService.completeEventByTime(1L, jwt);

    assertTrue(response.getIsCompleted());
    assertNotNull(response.getActualEndAt());
    assertEquals(response.getActualEndAt(), response.getCompletedAt());
    verify(xpService).addXpForTaskCompletion(user, Difficulty.MEDIUM, Category.STUDY);
    verify(userRepository).save(user);
    verify(eventRepository).save(event);
  }

  @Test
  void rejectsCompletingAnAlreadyCompletedEvent() {
    event.setIsCompleted(true);

    ResponsiveStatusException exception = assertThrows(
      ResponsiveStatusException.class,
      () -> eventService.completeEvent(1L, jwt)
    );

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    verify(xpService, org.mockito.Mockito.never())
      .addXpForTaskCompletion(user, Difficulty.MEDIUM, Category.STUDY);
  }
}
