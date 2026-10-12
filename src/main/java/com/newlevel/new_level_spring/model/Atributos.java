package com.newlevel.new_level_spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Atributos {
  @Column(name = "forca")
  private int forca;

  @Column(name = "destreza")
  private int destreza;

  @Column(name = "constituicao")
  private int constituicao;

  @Column(name = "inteligencia")
  private int inteligencia;

  @Column(name = "sabedoria")
  private int sabedoria;

  @Column(name = "carisma")
  private int carisma;

  public int sumLevels() {
    return forca + destreza + constituicao + inteligencia + sabedoria + carisma;
  }
}
