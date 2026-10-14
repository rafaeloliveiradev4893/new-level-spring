package com.newlevel.new_level_spring.model.DTOS;

import java.time.LocalDateTime;

import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDTO {
  @NotBlank(message = "O evento deve ter um título")
  @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
  private String title;

  @Size(max = 750, message = "A descrição deve ter no máximo 750 caracteres")
  private String description;

  @NotNull(message = "A data de início é obrigatória")
  private LocalDateTime startDate;

  @NotNull(message = "A data de término é obrigatória")
  private LocalDateTime endDate;

  @NotNull(message = "A categoria é obrigatória")
  private Category category;

  @NotNull(message = "A dificuldade é obrigatória")
  private Difficulty difficulty;
}