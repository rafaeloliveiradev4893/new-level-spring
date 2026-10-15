package com.newlevel.new_level_spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newlevel.new_level_spring.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByUserAuth0Id(String auth0Id);

    Optional<Task> findByIdAndUserAuth0Id(Long id, String auth0Id);

    void deleteAllByUser_Auth0Id(String auth0Id);
}