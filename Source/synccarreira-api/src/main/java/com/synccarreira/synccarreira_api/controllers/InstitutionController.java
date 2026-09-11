package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.InstitutionDTO;
import com.synccarreira.synccarreira_api.dto.InstitutionInsertDTO;
import com.synccarreira.synccarreira_api.dto.InstitutionUpdateDTO;
import com.synccarreira.synccarreira_api.services.InstitutionService;
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
@RequestMapping("/institutions")
@Tag(name = "Instituições", description = "Gestão de escolas e ONGs parceiras (RF-01). Somente administrador.")
@PreAuthorize("hasRole('ADMIN')")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    @GetMapping
    @Operation(summary = "Lista todas as instituições cadastradas.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Instituições encontradas com sucesso."))
    public ResponseEntity<List<InstitutionDTO>> findAll() {
        return ResponseEntity.ok(institutionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma instituição pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instituição encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada.")
    })
    public ResponseEntity<InstitutionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova instituição. CNPJ obrigatório, validado e único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Instituição criada com sucesso."),
            @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado."),
            @ApiResponse(responseCode = "422", description = "Dados inválidos (ex.: CNPJ inválido).")
    })
    public ResponseEntity<InstitutionDTO> create(@Valid @RequestBody InstitutionInsertDTO dto) {
        InstitutionDTO created = institutionService.create(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita uma instituição (inclui ativar/desativar).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instituição atualizada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada."),
            @ApiResponse(responseCode = "409", description = "CNPJ já pertence a outra instituição.")
    })
    public ResponseEntity<InstitutionDTO> update(@PathVariable Long id, @Valid @RequestBody InstitutionUpdateDTO dto) {
        return ResponseEntity.ok(institutionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma instituição sem turmas vinculadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Instituição excluída com sucesso."),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada."),
            @ApiResponse(responseCode = "409", description = "Instituição possui turmas vinculadas.")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        institutionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
