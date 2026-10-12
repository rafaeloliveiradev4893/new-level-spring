package com.newlevel.new_level_spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlevel.new_level_spring.model.Subtask;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {}
