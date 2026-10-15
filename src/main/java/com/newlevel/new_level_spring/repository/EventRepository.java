package com.newlevel.new_level_spring.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.newlevel.new_level_spring.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

  List<Event> findAllByUserAuth0Id(String auth0Id);

  void deleteAllByUser_Auth0Id(String auth0Id);

  @Query("""
    SELECT e FROM Event e
    WHERE e.user.auth0Id = :auth0Id
      AND e.startDate <= :end
      AND e.endDate >= :start
    ORDER BY e.startDate
    """)
  List<Event> findAllByUserAuth0IdAndPeriod(
    @Param("auth0Id") String auth0Id,
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end);
}
