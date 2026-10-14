package com.newlevel.new_level_spring.model.DTOS;

import java.time.LocalDate;
import java.util.List;

import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequestDTO {

  @NotBlank(message = "A tarefa deve ter um titulo")
  @Size(max = 100, message = "O Titulo deve ter no máximo 100 caracteres")
  private String title;

  @Size(max = 750, message = "Descriçao deve ter no máximo 750 caracteres")
  private String description;

  @NotNull(message = "A categoria é obrigatória")
  private Category category;

  @NotNull(message = "A dificuldade é obrigatória")
  private Difficulty difficulty;

  private LocalDate dueDate;

  private List<@Valid SubtaskRequestDTO> subtasks;
}
