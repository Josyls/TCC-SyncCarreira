package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.*;
import com.synccarreira.synccarreira_api.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/users")
@Tag(name = "Usuários", description = "Endpoints para interagir com os usuários da aplicação.")
public class UserController {

	@Autowired
	private UserService service;

	@Operation(summary = "Busca todos os usuários cadastrados.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Usuários encontrados com sucesso."
			)
	})
	@GetMapping
	public ResponseEntity<Page<UserDTO>> findAll(Pageable pageable) {
		Page<UserDTO> dto = service.findAll(pageable);
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Busca um usuário cadastrado com base no id.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Usuário encontrado com sucesso.",
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)) }
			),
			@ApiResponse(
					responseCode = "404",
					description = "Usuário não encontrado.",
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)) }
			)
	})
	@GetMapping(value = "/{id}")
	public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
		UserDTO dto = service.findById(id);
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Encontra o atual usuário logado.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Usuário encontrado com sucesso.",
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)) }
			),
			@ApiResponse(
					responseCode = "401",
					description = "Não autorizado porque o usuário não está logado.",
					content = @Content
			)
	})
	@GetMapping(value = "/me")
	public ResponseEntity<UserDTO> findMe() {
		UserDTO dto = service.findMe();
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Insere um novo usuário no banco de dados.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "201",
					description = "Usuário inserido com sucesso.",
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)) }
			),
			@ApiResponse(
					responseCode = "422",
					description = "Dados inválidos.",
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class)) }
			)
	})
	@PostMapping
	public ResponseEntity<UserDTO> insert(@Valid @RequestBody UserInsertDTO dto) {
		UserDTO result = service.insert(dto);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result.getId()).toUri();
		return ResponseEntity.created(uri).body(result);
	}

	@Operation(summary = "Atualiza um usuário já cadastrado no banco de dados com base no id.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Usuário atualizado com sucesso.",
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)) }
			),
			@ApiResponse(
					responseCode = "422",
					description = "Dados inválidos.",
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class)) }
			)
	})
	@PutMapping(value = "/{id}")
	public ResponseEntity<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
		UserDTO result = service.update(id, dto);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Deleta um usuário com base no id.")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "204",
					description = "Usuário deletado com sucesso.",
					content = @Content
			),
			@ApiResponse(
					responseCode = "404",
					description = "Recurso não encontrado.",
					content = { @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)) }
			)
	})
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
