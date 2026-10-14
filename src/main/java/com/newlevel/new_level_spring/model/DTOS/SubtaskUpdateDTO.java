package com.newlevel.new_level_spring.model.DTOS;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskUpdateDTO {
  @NotBlank(message = "O título da subtarefa é obrigatório")
  @Size(max = 100, message = "O título da subtarefa deve ter no máximo 100 caracteres")
  private String title;
}
