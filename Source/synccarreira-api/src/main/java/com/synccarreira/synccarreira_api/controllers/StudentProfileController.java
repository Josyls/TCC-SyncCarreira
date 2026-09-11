package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.StudentProfileDTO;
import com.synccarreira.synccarreira_api.dto.StudentProfileUpdateDTO;
import com.synccarreira.synccarreira_api.services.StudentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student-profile")
@Tag(name = "Perfil do aluno", description = "Informações de perfil e interesses do aluno (RF-14).")
@PreAuthorize("hasRole('USER')")
public class StudentProfileController {

    @Autowired
    private StudentProfileService studentProfileService;

    @GetMapping
    @Operation(summary = "Retorna o perfil do aluno logado e o catálogo de interesses.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK."))
    public ResponseEntity<StudentProfileDTO> getMine() {
        return ResponseEntity.ok(studentProfileService.getMine());
    }

    @PutMapping
    @Operation(summary = "Atualiza o perfil e os interesses do aluno logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado."),
            @ApiResponse(responseCode = "422", description = "Dados inválidos.")
    })
    public ResponseEntity<StudentProfileDTO> updateMine(@Valid @RequestBody StudentProfileUpdateDTO dto) {
        return ResponseEntity.ok(studentProfileService.updateMine(dto));
    }
}
