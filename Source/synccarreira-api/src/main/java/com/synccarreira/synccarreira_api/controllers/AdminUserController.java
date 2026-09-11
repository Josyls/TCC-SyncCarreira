package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.AccountStatusUpdateDTO;
import com.synccarreira.synccarreira_api.dto.ManagedUserDTO;
import com.synccarreira.synccarreira_api.dto.ManagedUserInsertDTO;
import com.synccarreira.synccarreira_api.dto.ManagedUserUpdateDTO;
import com.synccarreira.synccarreira_api.services.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Administração de usuários", description = "Criar, editar e desativar contas de alunos e psicólogas (RF-02). Somente administrador.")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Lista usuários gerenciados, com filtros opcionais.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Usuários encontrados com sucesso."))
    public ResponseEntity<List<ManagedUserDTO>> list(
            @RequestParam(required = false) String perfil,
            @RequestParam(required = false) Long institutionId,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(adminUserService.list(perfil, institutionId, active));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário gerenciado pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado.")
    })
    public ResponseEntity<ManagedUserDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cria uma conta de aluno ou psicóloga. CPF obrigatório e único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso."),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado."),
            @ApiResponse(responseCode = "422", description = "Dados inválidos.")
    })
    public ResponseEntity<ManagedUserDTO> create(@Valid @RequestBody ManagedUserInsertDTO dto) {
        ManagedUserDTO created = adminUserService.create(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita os dados de uma conta gerenciada (sem trocar o perfil).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado."),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já pertence a outra conta.")
    })
    public ResponseEntity<ManagedUserDTO> update(@PathVariable Long id, @Valid @RequestBody ManagedUserUpdateDTO dto) {
        return ResponseEntity.ok(adminUserService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Ativa ou desativa uma conta (RF-02).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado."),
            @ApiResponse(responseCode = "422", description = "Operação não permitida para este perfil.")
    })
    public ResponseEntity<ManagedUserDTO> setStatus(@PathVariable Long id, @Valid @RequestBody AccountStatusUpdateDTO dto) {
        return ResponseEntity.ok(adminUserService.setStatus(id, dto));
    }
}
