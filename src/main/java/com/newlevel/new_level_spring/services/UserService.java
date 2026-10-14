package com.newlevel.new_level_spring.services;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.newlevel.new_level_spring.exception.ResponsiveStatusException;
import com.newlevel.new_level_spring.model.DTOS.UserResponseDTO;
import com.newlevel.new_level_spring.model.User;
import com.newlevel.new_level_spring.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
  
  private final UserRepository userRepository;
  //Metodos externos
  public UserResponseDTO getCurrentUser(Jwt jwt) {

    String auth0Id = jwt.getSubject();

    User user = userRepository.findById(auth0Id)
      .orElseThrow(() ->
        new ResponsiveStatusException("Usuário não encontrado")
      );
    return toResponseDTO(user);
  }

  public UserResponseDTO createUser(Jwt jwt) {

      String auth0Id = jwt.getSubject();
      String name = jwt.getClaimAsString("name");

      if (existUserById(auth0Id)) {
          throw new ResponsiveStatusException(HttpStatus.CONFLICT, "Usuário já existe");
      }

      User newUser = User.builder()
          .auth0Id(auth0Id)
          .name(name)
          .build();

      User savedUser = userRepository.save(newUser);

      return toResponseDTO(savedUser);
  }

  public UserResponseDTO updateUser( Jwt jwt) {

    String auth0Id = jwt.getSubject();

    User user = userRepository.findById(auth0Id)
      .orElseThrow(() ->
        new ResponsiveStatusException("Usuário não encontrado")
      );

    String name = jwt.getClaimAsString("name"); //tá vindo como null


    user.setName(name);

    User updatedUser = userRepository.save(user);

    return toResponseDTO(updatedUser);
  }

  //Não é possivel deletar o Usuario
  public void deleteUser(Jwt jwt) {

    String auth0Id = jwt.getSubject();

    User user = getUserById(auth0Id);

    userRepository.delete(user);
  }

  //Metodos externos

  public User getUserById(String auth0Id){
    return userRepository.findById(auth0Id)
      .orElseThrow(() -> 
        new ResponsiveStatusException("User not found")
      );
  }

  public Boolean existUserById(String auth0Id){
    return userRepository.existsById(auth0Id);
  } 

  private UserResponseDTO toResponseDTO(User user) {
    return UserResponseDTO
      .builder()
      .name(user.getName())
      .level(user.getLevel())
      .atributos(user.getAtributos())
      .atributosXp(user.getAtributosXp())
      .createdAt(user.getCreatedAt())
      .build();
  }

}
