package com.newlevel.new_level_spring.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.newlevel.new_level_spring.exception.ResponsiveStatusException;
import com.newlevel.new_level_spring.model.DTOS.EventRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.SubtaskRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.TaskRequestDTO;
import com.newlevel.new_level_spring.model.DTOS.TaskUpdateDTO;
import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class DTOValidationTest {

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void closeValidatorFactory() {
    validatorFactory.close();
  }

  @Test
  void validatesOptionalTaskUpdateFieldsWhenProvided() {
    TaskUpdateDTO dto = new TaskUpdateDTO();
    dto.setTitle("   ");
    dto.setDescription("x".repeat(751));

    Set<String> invalidFields = validator.validate(dto).stream()
      .map(violation -> violation.getPropertyPath().toString())
      .collect(Collectors.toSet());

    assertTrue(invalidFields.contains("title"));
    assertTrue(invalidFields.contains("description"));
    assertFalse(validator.validate(new TaskUpdateDTO()).iterator().hasNext());
  }

  @Test
  void validatesEventFieldsAndCascadesIntoTaskSubtasks() {
    EventRequestDTO invalidEvent = new EventRequestDTO();
    TaskRequestDTO task = new TaskRequestDTO();
    task.setTitle("Task");
    task.setCategory(Category.OTHER);
    task.setDifficulty(Difficulty.EASY);
    task.setSubtasks(List.of(new SubtaskRequestDTO(" ")));

    Set<String> invalidEventFields = validator.validate(invalidEvent).stream()
      .map(violation -> violation.getPropertyPath().toString())
      .collect(Collectors.toSet());
    Set<String> invalidTaskFields = validator.validate(task).stream()
      .map(violation -> violation.getPropertyPath().toString())
      .collect(Collectors.toSet());

    assertTrue(invalidEventFields.containsAll(
      Set.of("title", "startDate", "endDate", "category", "difficulty")
    ));
    assertTrue(invalidTaskFields.contains("subtasks[0].title"));
  }

  @Test
  void mapsNotFoundConflictAndBadRequestToTheirHttpStatuses() {
    assertEquals(HttpStatus.NOT_FOUND, new ResponsiveStatusException("missing").getStatusCode());
    assertEquals(
      HttpStatus.CONFLICT,
      new ResponsiveStatusException(HttpStatus.CONFLICT, "already exists").getStatusCode()
    );
    assertEquals(
      HttpStatus.BAD_REQUEST,
      new ResponsiveStatusException(HttpStatus.BAD_REQUEST, "invalid").getStatusCode()
    );
  }
}
