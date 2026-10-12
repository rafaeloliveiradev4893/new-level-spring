package com.newlevel.new_level_spring.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.Jwt;

import com.newlevel.new_level_spring.model.DTOS.UserResponseDTO;
import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.repository.UserRepository;

public class UserServiceTest {

  @Mock 
  private UserRepository repository;

  @Mock
  private Jwt jwt;

  @InjectMocks 
  private UserService userService;

  @BeforeEach 
  void setup(){
    MockitoAnnotations.openMocks(this);
  }
  
  @Test 
  @DisplayName("Should get User succesfully when user exists") 
  void getCurrentUserCade1(){
    // Arrange
    String auth0Id = "auth|123";

    User user = User.builder()
      .auth0Id(auth0Id)
      .name("rafa")
      .build();

    when(jwt.getSubject()).thenReturn(auth0Id);
    when(repository.findById(auth0Id))
      .thenReturn(Optional.of(user));

    // Act
    UserResponseDTO response = userService.getCurrentUser(jwt);

    // Assert
    assertNotNull(response);
    //assertEquals(auth0Id, response.getAuth0Id());
    assertEquals("rafa", response.getName());
    assertNotNull(response.getAtributos());
    assertEquals(0, response.getAtributos().getForca());
    assertNotNull(response.getAtributosXp());
    assertEquals(0, response.getAtributosXp().getForca());
  }

  @Test 
  @DisplayName("Should throw exeption when user not exist") 
  void getCurrentUserCade2(){
    when(jwt.getSubject()).thenReturn(null);
    
  }
}
