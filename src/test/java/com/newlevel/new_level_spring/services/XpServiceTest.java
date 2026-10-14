package com.newlevel.new_level_spring.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.types.Category;
import com.newlevel.new_level_spring.types.Difficulty;

class XpServiceTest {

  private final XpService xpService = new XpService();

  @Test
  void addsXpToCategoryAttributeAndLevelsItEveryHundredXp() {
    User user = User.builder().auth0Id("user-1").build();

    XpResult firstCompletion =
      xpService.addXpForTaskCompletion(user, Difficulty.HARD, Category.FITNESS);

    assertFalse(firstCompletion.isLeveledUp());
    assertEquals(0, user.getLevel());
    assertEquals(40, user.getAtributosXp().getForca());
    assertEquals(0, user.getAtributos().getForca());

    xpService.addXpForTaskCompletion(user, Difficulty.HARD, Category.FITNESS);
    XpResult thirdCompletion =
      xpService.addXpForTaskCompletion(user, Difficulty.MEDIUM, Category.FITNESS);

    assertTrue(thirdCompletion.isLeveledUp());
    assertEquals(1, thirdCompletion.getLevelsGained());
    assertEquals(1, user.getLevel());
    assertEquals(1, user.getAtributos().getForca());
    assertEquals(0, user.getAtributosXp().getForca());
  }

  @Test
  void keepsXpAndLevelsIndependentBetweenAttributes() {
    User user = User.builder().auth0Id("user-1").build();

    for (int i = 0; i < 5; i++) {
      xpService.addXpForTaskCompletion(user, Difficulty.MEDIUM, Category.STUDY);
    }
    xpService.addXpForTaskCompletion(user, Difficulty.HARD, Category.SOCIAL);

    assertEquals(1, user.getAtributos().getInteligencia());
    assertEquals(0, user.getAtributosXp().getInteligencia());
    assertEquals(0, user.getAtributos().getCarisma());
    assertEquals(40, user.getAtributosXp().getCarisma());
    assertEquals(1, user.getLevel());
  }

  @Test
  void carriesExcessXpIntoTheNextLevelProgress() {
    User user = User.builder().auth0Id("user-1").build();

    for (int i = 0; i < 6; i++) {
      xpService.addXpForTaskCompletion(user, Difficulty.HARD, Category.HEALTH);
    }

    assertEquals(2, user.getAtributos().getConstituicao());
    assertEquals(40, user.getAtributosXp().getConstituicao());
    assertEquals(2, user.getLevel());
  }
}
