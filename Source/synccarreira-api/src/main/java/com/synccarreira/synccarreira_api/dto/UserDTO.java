package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

	@Getter
	private Long id;
	
	@NotBlank(message = "Campo obrigatório")
	@Getter
	private String name;
	
	@Email(message = "Favor entrar com email válido")
	@Getter
	private String email;

	@Getter
	Set<RoleDTO> roles = new HashSet<>();
	
	public UserDTO(User entity) {
		id = entity.getId();
		name = entity.getName();
		email = entity.getEmail();
		entity.getRoles().forEach(role -> this.roles.add(new RoleDTO(role)));
	}
}
