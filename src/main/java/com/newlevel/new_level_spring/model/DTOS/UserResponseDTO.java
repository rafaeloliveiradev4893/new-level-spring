package com.newlevel.new_level_spring.model.DTOS;

import java.time.LocalDateTime;

import com.newlevel.new_level_spring.model.Atributos;
import com.newlevel.new_level_spring.model.AtributosXp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {

    private String name;
    private int level;
    private Atributos atributos;
    private AtributosXp atributosXp;
    private LocalDateTime createdAt;
}