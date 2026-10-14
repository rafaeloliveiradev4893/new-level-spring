package com.newlevel.new_level_spring.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newlevel.new_level_spring.model.Subtask;
import com.newlevel.new_level_spring.model.DTOS.SubtaskRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.SubtaskResponseDTO;
import com.newlevel.new_level_spring.model.DTOS.SubtaskUpdateDTO;
import com.newlevel.new_level_spring.exception.ResponsiveStatusExeption;
import com.newlevel.new_level_spring.model.DTOS.TaskRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.TaskResponseDTO;
import com.newlevel.new_level_spring.model.DTOS.TaskUpdateDTO;
import com.newlevel.new_level_spring.model.Task;
import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.repository.TaskRepository;
import com.newlevel.new_level_spring.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  private final XpService xpService;

  @Transactional
  public TaskResponseDTO createTask(Jwt jwt, TaskRequestDTO dto) {
    User user = userRepository.findById(jwt.getSubject())
      .orElseThrow(() -> new ResponsiveStatusExeption("Usuário não encontrado"));

    Task task = new Task();
    task.setTitle(dto.getTitle());
    task.setDescription(dto.getDescription());
    task.setUser(user);
    task.setDueDate(dto.getDueDate());
    task.setCategory(dto.getCategory());
    task.setDifficulty(dto.getDifficulty());

    if (dto.getSubtasks() != null) {
      dto.getSubtasks().forEach(s -> task.addSubtask(buildSubtask(s)));
    }

    taskRepository.save(task);
    return toResponseDTO(task);
  }

  @Transactional
  public TaskResponseDTO addSubtask(Long taskId, Jwt jwt, SubtaskRequestDTO dto) {
    Task task = findTaskOwnedByUser(taskId, jwt.getSubject());

    if (task.getIsCompleted()) {
      throw new ResponsiveStatusExeption("Não é possível adicionar subtarefas a uma tarefa concluída");
    }

    task.addSubtask(buildSubtask(dto));
    return toResponseDTO(taskRepository.save(task));
  }

  @Transactional
  public TaskResponseDTO completeSubtask(Long taskId, Long subtaskId, Jwt jwt) {
    Task task = findTaskOwnedByUser(taskId, jwt.getSubject());
    Subtask subtask = findSubtaskInTask(task, subtaskId);

    if (subtask.getIsCompleted()) {
      throw new ResponsiveStatusExeption("A subtarefa já foi completa");
    }

    subtask.setIsCompleted(true);
    subtask.setCompletedAt(LocalDateTime.now());

    if (!task.getSubtasks().isEmpty()
        && task.getSubtasks().stream().allMatch(
          currentSubtask -> Boolean.TRUE.equals(currentSubtask.getIsCompleted()))) {
      LocalDateTime completedAt = LocalDateTime.now();
      task.setIsCompleted(true);
      task.setCompletedAt(completedAt);
      task.setUpdatedAt(completedAt);
    }

    return toResponseDTO(taskRepository.save(task));
  }

  @Transactional
  public TaskResponseDTO deleteSubtask(Long taskId, Long subtaskId, Jwt jwt) {
    Task task = findTaskOwnedByUser(taskId, jwt.getSubject());
    Subtask subtask = findSubtaskInTask(task, subtaskId);

    task.removeSubtask(subtask); // orphanRemoval apaga do banco
    return toResponseDTO(taskRepository.save(task));
  }

  @Transactional
  public TaskResponseDTO updateSubtask(Long taskId, Long subtaskId, Jwt jwt, SubtaskUpdateDTO dto) {
    Task task = findTaskOwnedByUser(taskId, jwt.getSubject());
    Subtask subtask = findSubtaskInTask(task, subtaskId);

    if (dto.getTitle() == null || dto.getTitle().isBlank()) {
      throw new ResponsiveStatusExeption("O título da subtarefa é obrigatório");
    }

    subtask.setTitle(dto.getTitle().trim());
    return toResponseDTO(taskRepository.save(task));
  }

  public List<TaskResponseDTO> listTasksByUser(Jwt jwt) {
    String auth0Id = jwt.getSubject();
    return taskRepository.findAllByUserAuth0Id(auth0Id)
      .stream()
      .map(this::toResponseDTO)
      .collect(Collectors.toList());
  }

  public TaskResponseDTO getTaskById(Long taskId, Jwt jwt) {
    String auth0Id = jwt.getSubject();

    Task task = findTaskOwnedByUser(taskId, auth0Id);
    return toResponseDTO(task);
  }

  @Transactional
  public TaskResponseDTO updateTask(Long taskId, Jwt jwt, TaskRequestDTO dto) {
    String auth0Id = jwt.getSubject();
    Task task = findTaskOwnedByUser(taskId, auth0Id);

    task.setTitle(dto.getTitle());
    task.setDescription(dto.getDescription());
    task.setDueDate(dto.getDueDate());
    task.setCategory(dto.getCategory());
    task.setDifficulty(dto.getDifficulty());
    task.setUpdatedAt(LocalDateTime.now());

    return toResponseDTO(taskRepository.save(task));
  }

  @Transactional
  public TaskResponseDTO updatePartialTask(Long taskId, Jwt jwt, TaskUpdateDTO dto) {
    String auth0Id = jwt.getSubject();

    Task task = findTaskOwnedByUser(taskId, auth0Id);

    if (dto.getTitle() != null) {
      task.setTitle(dto.getTitle());
    }

    if (dto.getDescription() != null) {
      task.setDescription(dto.getDescription());
    }

    if (dto.getDueDate() != null) {
      task.setDueDate(dto.getDueDate());
    }

    if (dto.getCategory() != null) {
      task.setCategory(dto.getCategory());
    }

    if (dto.getDifficulty() != null) {
      task.setDifficulty(dto.getDifficulty());
    }

    task.setUpdatedAt(LocalDateTime.now());

    return toResponseDTO(taskRepository.save(task));
  }

  @Transactional
  public TaskResponseDTO completeTask(Long taskId, Jwt jwt) {
    String auth0Id = jwt.getSubject();
    User user = userRepository.findById(jwt.getSubject())
      .orElseThrow(() -> new ResponsiveStatusExeption("Usuário não encontrado"));
    
    Task task = findTaskOwnedByUser(taskId, auth0Id);

    if (task.getIsCompleted()) {
      throw new ResponsiveStatusExeption("A tarefa já foi completa");
    }

    task.setCompletedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());
    task.setIsCompleted(true);

    LocalDateTime completedAt = task.getCompletedAt();
    task.getSubtasks().forEach(subtask -> {
      subtask.setIsCompleted(true);
      if (subtask.getCompletedAt() == null) {
        subtask.setCompletedAt(completedAt);
      }
    });
    
    xpService.addXpForTaskCompletion(user, task.getDifficulty(), task.getCategory());
    userRepository.save(user);

    return toResponseDTO(taskRepository.save(task));
  }

  @Transactional
  public void deleteTask(Long taskId, Jwt jwt) {
    String auth0Id = jwt.getSubject();

    Task task = findTaskOwnedByUser(taskId, auth0Id);
    taskRepository.delete(task);
  }

  private Task findTaskOwnedByUser(Long taskId, String auth0Id) {

    Task task = taskRepository.findById(taskId)
      .orElseThrow(() -> new ResponsiveStatusExeption("Tarefa não encontrada"));

    if (!task.getUser().getAuth0Id().equals(auth0Id)) {
      throw new ResponsiveStatusExeption("Tarefa não encontrada");
    }
    return task;
  }

  private Subtask buildSubtask(SubtaskRequestDTO dto) {
    if (dto.getTitle() == null || dto.getTitle().isBlank()) {
      throw new ResponsiveStatusExeption("O título da subtarefa é obrigatório");
    }
    return Subtask.builder().title(dto.getTitle().trim()).build();
  }

  private Subtask findSubtaskInTask(Task task, Long subtaskId) {
    return task.getSubtasks().stream()
      .filter(s -> s.getId().equals(subtaskId))
      .findFirst()
      .orElseThrow(() -> new ResponsiveStatusExeption("Subtarefa não encontrada"));
  }

  public TaskResponseDTO toResponseDTO(Task task) {
    return TaskResponseDTO.builder()
      .category(task.getCategory())
      .completedAt(task.getCompletedAt())
      .createdAt(task.getCreatedAt())
      .description(task.getDescription())
      .difficulty(task.getDifficulty())
      .dueDate(task.getDueDate())
      .id(task.getId())
      .title(task.getTitle())
      .updatedAt(task.getUpdatedAt())
      .isCompleted(task.getIsCompleted())
      .subtasks(task.getSubtasks().stream()
        .map(s -> SubtaskResponseDTO.builder()
          .id(s.getId())
          .title(s.getTitle())
          .isCompleted(s.getIsCompleted())
          .completedAt(s.getCompletedAt())
          .build())
        .collect(Collectors.toList()))
      .build();
  }
}