package com.newlevel.new_level_spring.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

import com.newlevel.new_level_spring.exception.ResponsiveStatusException;
import com.newlevel.new_level_spring.model.DTOS.SubtaskRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.SubtaskUpdateDTO;
import com.newlevel.new_level_spring.model.Subtask;
import com.newlevel.new_level_spring.model.Task;
import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.repository.TaskRepository;
import com.newlevel.new_level_spring.repository.UserRepository;
import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

class TaskServiceTest {

  private final TaskRepository taskRepository = mock(TaskRepository.class);
  private final UserRepository userRepository = mock(UserRepository.class);
  private final XpService xpService = mock(XpService.class);
  private final TaskService taskService = new TaskService(taskRepository, userRepository, xpService);
  private final Jwt jwt = Jwt.withTokenValue("token")
    .header("alg", "none")
    .subject("user-1")
    .build();

  private User user;
  private Task task;

  @BeforeEach
  void setUp() {
    user = User.builder().auth0Id("user-1").build();
    task = Task.builder()
      .id(1L)
      .user(user)
      .title("Task")
      .category(Category.OTHER)
      .difficulty(Difficulty.EASY)
      .subtasks(new ArrayList<>())
      .build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
  }

  @Test
  void completesParentTaskWhenAllSubtasksAreCompleted() {
    Subtask firstSubtask = addSubtask(1L);
    Subtask lastSubtask = addSubtask(2L);

    taskService.completeSubtask(1L, firstSubtask.getId(), jwt);
    assertFalse(task.getIsCompleted());

    taskService.completeSubtask(1L, lastSubtask.getId(), jwt);

    assertTrue(task.getIsCompleted());
    assertNotNull(task.getCompletedAt());
  }

  @Test
  void completesAllSubtasksWhenParentTaskIsCompleted() {
    Subtask firstSubtask = addSubtask(1L);
    Subtask secondSubtask = addSubtask(2L);

    taskService.completeTask(1L, jwt);

    assertTrue(task.getIsCompleted());
    assertTrue(firstSubtask.getIsCompleted());
    assertTrue(secondSubtask.getIsCompleted());
    assertNotNull(firstSubtask.getCompletedAt());
    assertNotNull(secondSubtask.getCompletedAt());
  }

  @Test
  void hidesTasksOwnedByAnotherUser() {
    TaskService taskServiceForAnotherUser =
      new TaskService(taskRepository, userRepository, xpService);
    Jwt otherUserJwt = Jwt.withTokenValue("token")
      .header("alg", "none")
      .subject("user-2")
      .build();

    ResponsiveStatusException exception = assertThrows(
      ResponsiveStatusException.class,
      () -> taskServiceForAnotherUser.getTaskById(1L, otherUserJwt)
    );

    assertTrue(exception.getMessage().contains("Tarefa não encontrada"));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void completingTaskAwardsXpAndPersistsUserAndTask() {
    taskService.completeTask(1L, jwt);

    verify(xpService).addXpForTaskCompletion(user, Difficulty.EASY, Category.OTHER);
    verify(userRepository).save(user);
    verify(taskRepository).save(task);
  }

  @Test
  void rejectsCompletingAnAlreadyCompletedSubtask() {
    Subtask subtask = addSubtask(1L);
    subtask.setIsCompleted(true);

    ResponsiveStatusException exception = assertThrows(
      ResponsiveStatusException.class,
      () -> taskService.completeSubtask(1L, subtask.getId(), jwt)
    );

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
  }

  @Test
  void addsUpdatesAndDeletesSubtasks() {
    SubtaskRequestDTO request = SubtaskRequestDTO.builder().title("New subtask").build();

    taskService.addSubtask(1L, jwt, request);
    Subtask subtask = task.getSubtasks().get(0);
    subtask.setId(1L);
    taskService.updateSubtask(
      1L,
      subtask.getId(),
      jwt,
      SubtaskUpdateDTO.builder().title("Updated subtask").build()
    );

    assertEquals("Updated subtask", subtask.getTitle());
    taskService.deleteSubtask(1L, subtask.getId(), jwt);

    assertTrue(task.getSubtasks().isEmpty());
    verify(taskRepository, org.mockito.Mockito.times(3)).save(task);
  }

  private Subtask addSubtask(Long id) {
    Subtask subtask = Subtask.builder().id(id).title("Subtask " + id).build();
    task.addSubtask(subtask);
    return subtask;
  }
}
