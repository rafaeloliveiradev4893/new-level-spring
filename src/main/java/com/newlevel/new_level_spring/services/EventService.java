package com.newlevel.new_level_spring.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlevel.new_level_spring.exception.ResponsiveStatusException;
import com.newlevel.new_level_spring.model.DTOS.EventRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.EventResponseDTO;
import com.newlevel.new_level_spring.model.DTOS.EventUpdateDTO;
import com.newlevel.new_level_spring.model.Event;
import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.repository.EventRepository;
import com.newlevel.new_level_spring.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final UserRepository userRepository;

  private final XpService xpService;

  @Transactional
  public EventResponseDTO createEvent(Jwt jwt, EventRequestDTO dto) {
    User user = userRepository.findById(jwt.getSubject())
      .orElseThrow(() -> new ResponsiveStatusException("Usuário não encontrado"));

    validateDates(dto.getStartDate(), dto.getEndDate());

    Event event = new Event();
    event.setTitle(dto.getTitle());
    event.setDescription(dto.getDescription());
    event.setUser(user);
    event.setStartDate(dto.getStartDate());
    event.setEndDate(dto.getEndDate());
    event.setCategory(dto.getCategory());
    event.setDifficulty(dto.getDifficulty());
    eventRepository.save(event);
    return toResponseDTO(event);
  }

  public List<EventResponseDTO> listEventsByUser(Jwt jwt) {
    String auth0Id = jwt.getSubject();
    return eventRepository.findAllByUserAuth0Id(auth0Id)
      .stream()
      .map(this::toResponseDTO)
      .collect(Collectors.toList());
  }

  public List<EventResponseDTO> listEventsByPeriod(Jwt jwt, LocalDateTime start, LocalDateTime end) {
    String auth0Id = jwt.getSubject();
    validateDates(start, end);

    return eventRepository.findAllByUserAuth0IdAndPeriod(auth0Id, start, end)
      .stream()
      .map(this::toResponseDTO)
      .collect(Collectors.toList());
  }

  @Transactional
  public EventResponseDTO startEvent(Long eventId, Jwt jwt) {
    String auth0Id = jwt.getSubject();

    Event event = findEventOwnedByUser(eventId, auth0Id);

    if (event.getIsCompleted()) {
      throw new ResponsiveStatusException(
        HttpStatus.CONFLICT,
        "Não é possível iniciar um evento concluído"
      );
    }

    if (event.getActualStartAt() != null) {
      throw new ResponsiveStatusException(
        HttpStatus.CONFLICT,
        "O evento já foi iniciado"
      );
    }

    event.setActualStartAt(LocalDateTime.now());

    return toResponseDTO(eventRepository.save(event));
  }

  public EventResponseDTO getEventById(Long eventId, Jwt jwt) {
    String auth0Id = jwt.getSubject();

    Event event = findEventOwnedByUser(eventId, auth0Id);
    return toResponseDTO(event);
  }

  @Transactional
  public EventResponseDTO updateEvent(Long eventId, Jwt jwt, EventRequestDTO dto) {
    String auth0Id = jwt.getSubject();
    Event event = findEventOwnedByUser(eventId, auth0Id);

    validateDates(dto.getStartDate(), dto.getEndDate());

    event.setTitle(dto.getTitle());
    event.setDescription(dto.getDescription());
    event.setStartDate(dto.getStartDate());
    event.setEndDate(dto.getEndDate());
    event.setCategory(dto.getCategory());
    event.setDifficulty(dto.getDifficulty());

    return toResponseDTO(eventRepository.save(event));
  }

  @Transactional
  public EventResponseDTO updatePartialEvent(Long eventId, Jwt jwt, EventUpdateDTO dto) {
    String auth0Id = jwt.getSubject();

    Event event = findEventOwnedByUser(eventId, auth0Id);

    if (dto.getTitle() != null) {
      event.setTitle(dto.getTitle());
    }

    if (dto.getDescription() != null) {
      event.setDescription(dto.getDescription());
    }

    if (dto.getStartDate() != null) {
      event.setStartDate(dto.getStartDate());
    }

    if (dto.getEndDate() != null) {
      event.setEndDate(dto.getEndDate());
    }

    if (dto.getCategory() != null) {
      event.setCategory(dto.getCategory());
    }

    if (dto.getDifficulty() != null) {
      event.setDifficulty(dto.getDifficulty());
    }

    // valida com os valores finais (misturando o que veio e o que já existia)
    validateDates(event.getStartDate(), event.getEndDate());

    return toResponseDTO(eventRepository.save(event));
  }

  @Transactional
  public EventResponseDTO completeEvent(Long eventId, Jwt jwt) {
    String auth0Id = jwt.getSubject();
    User user = userRepository.findById(auth0Id)
      .orElseThrow(() -> new ResponsiveStatusException("Usuário não encontrado"));

    Event event = findEventOwnedByUser(eventId, auth0Id);

    if (event.getIsCompleted()) {
      throw new ResponsiveStatusException(HttpStatus.CONFLICT, "O evento já foi completo");
    }

    event.setCompletedAt(LocalDateTime.now());
    event.setIsCompleted(true);

    xpService.addXpForTaskCompletion(user, event.getDifficulty(), event.getCategory());
    userRepository.save(user);

    return toResponseDTO(eventRepository.save(event));
  }

  @Transactional
  public EventResponseDTO completeEventByTime(Long eventId, Jwt jwt) {
    String auth0Id = jwt.getSubject();
    User user = userRepository.findById(auth0Id)
      .orElseThrow(() -> new ResponsiveStatusException("Usuário não encontrado"));

    Event event = findEventOwnedByUser(eventId, auth0Id);

    if (event.getIsCompleted()) {
      throw new ResponsiveStatusException(HttpStatus.CONFLICT, "O evento já foi completo");
    }

    event.setActualEndAt(LocalDateTime.now());
    event.setCompletedAt(event.getActualEndAt());
    event.setIsCompleted(true);

    xpService.addXpForTaskCompletion(user, event.getDifficulty(), event.getCategory());
    userRepository.save(user);

    return toResponseDTO(eventRepository.save(event));
  }

  @Transactional
  public void deleteEvent(Long eventId, Jwt jwt) {
    String auth0Id = jwt.getSubject();

    Event event = findEventOwnedByUser(eventId, auth0Id);
    eventRepository.delete(event);
  }

  private Event findEventOwnedByUser(Long eventId, String auth0Id) {

    Event event = eventRepository.findById(eventId)
      .orElseThrow(() -> new ResponsiveStatusException("Evento não encontrado"));

    if (!event.getUser().getAuth0Id().equals(auth0Id)) {
      throw new ResponsiveStatusException("Evento não encontrado");
    }
    return event;
  }

  private void validateDates(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new ResponsiveStatusException(
        HttpStatus.BAD_REQUEST,
        "As datas de início e término são obrigatórias"
      );
    }

    if (end.isBefore(start)) {
      throw new ResponsiveStatusException(
        HttpStatus.BAD_REQUEST,
        "A data de término não pode ser anterior à data de início"
      );
    }
  }

  public EventResponseDTO toResponseDTO(Event event) {
    return EventResponseDTO.builder()
      .id(event.getId())
      .title(event.getTitle())
      .description(event.getDescription())
      .startDate(event.getStartDate())
      .endDate(event.getEndDate())
      .category(event.getCategory())
      .difficulty(event.getDifficulty())
      .isCompleted(event.getIsCompleted())
      .completedAt(event.getCompletedAt())
      .createdAt(event.getCreatedAt())
      .updatedAt(event.getUpdatedAt())
      .actualStartAt(event.getActualStartAt())
      .actualEndAt(event.getActualEndAt())
      .build();
  }
}