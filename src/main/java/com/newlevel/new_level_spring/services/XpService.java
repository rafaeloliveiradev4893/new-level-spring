package com.newlevel.new_level_spring.services;


import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.newlevel.new_level_spring.model.Atributos;
import com.newlevel.new_level_spring.model.AtributosXp;
import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

@Service
@RequiredArgsConstructor
public class XpService {

    private static final int XP_EASY = 10;
    private static final int XP_MEDIUM = 20;
    private static final int XP_HARD = 40;

    public XpResult addXpForTaskCompletion(User user, Difficulty difficulty, Category category) {
      int xpGained = switch (difficulty) {
        case EASY -> XP_EASY;
        case MEDIUM -> XP_MEDIUM;
        case HARD -> XP_HARD;
      };

      Attribute attribute = attributeForCategory(category);
      AtributosXp atributosXp = user.getAtributosXp();
      int accumulatedXp = getXp(atributosXp, attribute) + xpGained;
      int levelsGained = accumulatedXp / 100;
      setXp(atributosXp, attribute, accumulatedXp % 100);

      Atributos atributos = user.getAtributos();
      increaseAttributeLevel(atributos, attribute, levelsGained);

      int previousLevel = user.getLevel();
      user.setLevel(atributos.sumLevels());
      int userLevelsGained = user.getLevel() - previousLevel;
      return new XpResult(userLevelsGained > 0, user.getLevel(), userLevelsGained);
    }

    private Attribute attributeForCategory(Category category) {
      return switch (category) {
        case FITNESS -> Attribute.FORCA;
        case ROUTINE, PRODUCTIVITY, HOBBIES -> Attribute.DESTREZA;
        case HEALTH, NUTRITION, SLEEP, SELF_CARE -> Attribute.CONSTITUICAO;
        case STUDY, WORK, FINANCE -> Attribute.INTELIGENCIA;
        case MENTAL_HEALTH, PERSONAL_GROWTH, SPIRITUALITY, OTHER, UNDEFINED -> Attribute.SABEDORIA;
        case SOCIAL -> Attribute.CARISMA;
      };
    }

    private int getXp(AtributosXp atributosXp, Attribute attribute) {
      return switch (attribute) {
        case FORCA -> atributosXp.getForca();
        case DESTREZA -> atributosXp.getDestreza();
        case CONSTITUICAO -> atributosXp.getConstituicao();
        case INTELIGENCIA -> atributosXp.getInteligencia();
        case SABEDORIA -> atributosXp.getSabedoria();
        case CARISMA -> atributosXp.getCarisma();
      };
    }

    private void setXp(AtributosXp atributosXp, Attribute attribute, int xp) {
      switch (attribute) {
        case FORCA -> atributosXp.setForca(xp);
        case DESTREZA -> atributosXp.setDestreza(xp);
        case CONSTITUICAO -> atributosXp.setConstituicao(xp);
        case INTELIGENCIA -> atributosXp.setInteligencia(xp);
        case SABEDORIA -> atributosXp.setSabedoria(xp);
        case CARISMA -> atributosXp.setCarisma(xp);
      }
    }

    private void increaseAttributeLevel(Atributos atributos, Attribute attribute, int levelsGained) {
      switch (attribute) {
        case FORCA -> atributos.setForca(atributos.getForca() + levelsGained);
        case DESTREZA -> atributos.setDestreza(atributos.getDestreza() + levelsGained);
        case CONSTITUICAO -> atributos.setConstituicao(atributos.getConstituicao() + levelsGained);
        case INTELIGENCIA -> atributos.setInteligencia(atributos.getInteligencia() + levelsGained);
        case SABEDORIA -> atributos.setSabedoria(atributos.getSabedoria() + levelsGained);
        case CARISMA -> atributos.setCarisma(atributos.getCarisma() + levelsGained);
      }
    }

    private enum Attribute {
      FORCA,
      DESTREZA,
      CONSTITUICAO,
      INTELIGENCIA,
      SABEDORIA,
      CARISMA
    }
}