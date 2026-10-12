package com.newlevel.new_level_spring.model.DTOS;

import java.time.LocalDateTime;

import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
  private Long id;
  private String title;
  private String description;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Category category;
  private Difficulty difficulty;
  private Boolean isCompleted;
  private LocalDateTime completedAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime actualStartAt;
  private LocalDateTime actualEndAt;
}