package com.newlevel.new_level_spring.model.DTOS;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskUpdateDTO {
  @NotBlank 
  private String title;
}
