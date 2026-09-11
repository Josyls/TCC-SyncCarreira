package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.SchoolClassDTO;
import com.synccarreira.synccarreira_api.dto.SchoolClassInsertDTO;
import com.synccarreira.synccarreira_api.dto.SchoolClassUpdateDTO;
import com.synccarreira.synccarreira_api.services.SchoolClassService;
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
@RequestMapping("/classes")
@Tag(name = "Turmas", description = "Gestão de turmas vinculadas às instituições (RF-01). Somente administrador.")
@PreAuthorize("hasRole('ADMIN')")
public class SchoolClassController {

    @Autowired
    private SchoolClassService schoolClassService;

    @GetMapping
    @Operation(summary = "Lista turmas. Filtro opcional por instituição.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Turmas encontradas com sucesso."))
    public ResponseEntity<List<SchoolClassDTO>> findAll(
            @RequestParam(value = "institutionId", required = false) Long institutionId) {
        List<SchoolClassDTO> result = institutionId != null
                ? schoolClassService.findByInstitution(institutionId)
                : schoolClassService.findAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma turma pelo id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada.")
    })
    public ResponseEntity<SchoolClassDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(schoolClassService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cria uma turma vinculada a uma instituição.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turma criada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Instituição ou psicóloga não encontrada."),
            @ApiResponse(responseCode = "422", description = "Dados inválidos.")
    })
    public ResponseEntity<SchoolClassDTO> create(@Valid @RequestBody SchoolClassInsertDTO dto) {
        SchoolClassDTO created = schoolClassService.create(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita uma turma (inclui ativar/desativar e trocar a psicóloga).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma atualizada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada.")
    })
    public ResponseEntity<SchoolClassDTO> update(@PathVariable Long id, @Valid @RequestBody SchoolClassUpdateDTO dto) {
        return ResponseEntity.ok(schoolClassService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma turma.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Turma excluída com sucesso."),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada.")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        schoolClassService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
