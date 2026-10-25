package com.newlevel.new_level_spring.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "subtasks")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Entity
@Builder
public class Subtask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Task task;

  @Column(name = "title", nullable = false)
  private String title;

  @Builder.Default
  @Column(name = "is_completed")
  private Boolean isCompleted = false;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

}