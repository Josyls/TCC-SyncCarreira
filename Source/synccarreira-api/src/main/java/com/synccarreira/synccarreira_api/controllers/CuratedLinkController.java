package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.psicologa.CuratedLinkDTOs;
import com.synccarreira.synccarreira_api.services.CuratedLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/curated-links")
@Tag(name = "Links curados", description = "Fontes externas confiáveis exibidas na trilha de Informação (RF-06, RN-05).")
public class CuratedLinkController {

    @Autowired
    private CuratedLinkService curatedLinkService;

    @GetMapping
    @Operation(summary = "Lista os links cadastrados pela psicóloga logada.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<List<CuratedLinkDTOs.View>> listMine() {
        return ResponseEntity.ok(curatedLinkService.listMine());
    }

    @GetMapping("/for-me")
    @Operation(summary = "Lista os links ativos visíveis para o aluno logado (globais + da instituição).")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CuratedLinkDTOs.View>> forStudent() {
        return ResponseEntity.ok(curatedLinkService.listForStudent());
    }

    @GetMapping("/institutions")
    @Operation(summary = "Instituições que podem ser associadas a um link (escopo do usuário).")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<List<CuratedLinkDTOs.InstitutionOption>> institutionOptions() {
        return ResponseEntity.ok(curatedLinkService.institutionOptions());
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo link curado.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<CuratedLinkDTOs.View> create(@Valid @RequestBody CuratedLinkDTOs.Insert dto) {
        return ResponseEntity.status(201).body(curatedLinkService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita um link curado.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<CuratedLinkDTOs.View> update(@PathVariable Long id, @Valid @RequestBody CuratedLinkDTOs.Update dto) {
        return ResponseEntity.ok(curatedLinkService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um link curado.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        curatedLinkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
