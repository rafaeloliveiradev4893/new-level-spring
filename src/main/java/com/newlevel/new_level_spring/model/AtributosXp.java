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
public class AtributosXp {
  @Column(name = "xp_forca")
  private int forca;

  @Column(name = "xp_destreza")
  private int destreza;

  @Column(name = "xp_constituicao")
  private int constituicao;

  @Column(name = "xp_inteligencia")
  private int inteligencia;

  @Column(name = "xp_sabedoria")
  private int sabedoria;

  @Column(name = "xp_carisma")
  private int carisma;
}
