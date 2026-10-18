package com.newlevel.new_level_spring.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@AllArgsConstructor
@Data
@Table(name = "users")
@NoArgsConstructor
@Builder
public class User {
  @Id
  @Column(name = "auth0_id", nullable = false, updatable = false)
  private String auth0Id;

  @Column(name = "name")
  private String name;

  @Builder.Default
  @Column(name = "level", nullable = false)
  private int level = 0;

  @Builder.Default
  @Embedded
  private Atributos atributos = new Atributos();

  @Builder.Default
  @Embedded
  private AtributosXp atributosXp = new AtributosXp();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt; // Quando foi criado
  
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt; // Última atualização

  @Version 
  private Long version;

}
