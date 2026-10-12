package com.newlevel.new_level_spring.model.DTOS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Category category;
    private Difficulty difficulty;
    private Boolean isCompleted;
    private List<SubtaskResponseDTO> subtasks;
}
