package com.synccarreira.synccarreira_api.dto;

import com.synccarreira.synccarreira_api.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

	@Getter
	private Long id;

	@Getter
	private String authority;
	
	public RoleDTO(Role role) {
		id = role.getId();
		authority = role.getAuthority();
	}
}
