package com.newlevel.new_level_spring.model.DTOS;

import java.time.LocalDate;

import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskUpdateDTO {
  @Pattern(regexp = "(?s).*\\S.*", message = "O título não pode estar em branco")
  @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
  private String title;

  @Size(max = 750, message = "A descrição deve ter no máximo 750 caracteres")
  private String description;

  private LocalDate dueDate;
  private Category category;
  private Difficulty difficulty;
}
