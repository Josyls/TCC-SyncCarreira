package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Student;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    @Getter
    private Long id;

    @NotBlank(message = "Campo obrigatório")
    @Getter
    private String name;

    @Email(message = "Favor entrar com email válido")
    @Getter
    private String email;

    @Getter
    private Set<RoleDTO> roles = new HashSet<>();

    @Getter
    private String schollarYear;

    @Getter
    private String schoolType;

    public StudentDTO(Student entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        entity.getRoles().forEach(role -> this.roles.add(new RoleDTO(role)));
        this.schollarYear = entity.getSchollarYear();
        this.schoolType = entity.getSchoolType();
    }
}