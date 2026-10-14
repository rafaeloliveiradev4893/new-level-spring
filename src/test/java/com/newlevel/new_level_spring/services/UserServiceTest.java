package com.newlevel.new_level_spring.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

import com.newlevel.new_level_spring.exception.ResponsiveStatusException;
import com.newlevel.new_level_spring.model.DTOS.UserResponseDTO;
import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository repository;

  @Mock
  private Jwt jwt;

  @InjectMocks
  private UserService userService;

  @Test
  void returnsCurrentUserAndAttributes() {
    String auth0Id = "auth|123";
    User user = User.builder().auth0Id(auth0Id).name("rafa").build();
    when(jwt.getSubject()).thenReturn(auth0Id);
    when(repository.findById(auth0Id)).thenReturn(Optional.of(user));

    UserResponseDTO response = userService.getCurrentUser(jwt);

    assertNotNull(response);
    assertEquals("rafa", response.getName());
    assertEquals(0, response.getLevel());
    assertNotNull(response.getAtributos());
    assertEquals(0, response.getAtributos().getForca());
    assertNotNull(response.getAtributosXp());
    assertEquals(0, response.getAtributosXp().getForca());
  }

  @Test
  void throwsNotFoundWhenCurrentUserDoesNotExist() {
    when(jwt.getSubject()).thenReturn("missing-user");
    when(repository.findById("missing-user")).thenReturn(Optional.empty());

    ResponsiveStatusException exception = assertThrows(
      ResponsiveStatusException.class,
      () -> userService.getCurrentUser(jwt)
    );

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void createsUserFromJwtClaims() {
    when(jwt.getSubject()).thenReturn("auth|123");
    when(jwt.getClaimAsString("name")).thenReturn("Rafa");
    when(repository.existsById("auth|123")).thenReturn(false);
    when(repository.save(org.mockito.ArgumentMatchers.any(User.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    UserResponseDTO response = userService.createUser(jwt);

    assertEquals("Rafa", response.getName());
    verify(repository).save(org.mockito.ArgumentMatchers.argThat(
      user -> "auth|123".equals(user.getAuth0Id()) && "Rafa".equals(user.getName())
    ));
  }

  @Test
  void rejectsCreatingAnExistingUser() {
    when(jwt.getSubject()).thenReturn("auth|123");
    when(repository.existsById("auth|123")).thenReturn(true);

    ResponsiveStatusException exception = assertThrows(
      ResponsiveStatusException.class,
      () -> userService.createUser(jwt)
    );

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
  }

  @Test
  void updatesAndDeletesExistingUser() {
    User user = User.builder().auth0Id("auth|123").name("Old").build();
    when(jwt.getSubject()).thenReturn("auth|123");
    when(jwt.getClaimAsString("name")).thenReturn("New");
    when(repository.findById("auth|123")).thenReturn(Optional.of(user));
    when(repository.save(user)).thenReturn(user);

    UserResponseDTO response = userService.updateUser(jwt);
    userService.deleteUser(jwt);

    assertEquals("New", response.getName());
    verify(repository).delete(user);
  }
}
