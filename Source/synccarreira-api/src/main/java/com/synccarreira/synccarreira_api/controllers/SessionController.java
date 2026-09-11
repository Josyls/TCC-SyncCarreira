package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.psicologa.SessionDTOs;
import com.synccarreira.synccarreira_api.services.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Agendamento de sessões", description = "Sessões individuais/grupo com convite .ics (RF-08); área do aluno (RF-16).")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    // -------- psicóloga --------

    @GetMapping
    @Operation(summary = "Lista as sessões criadas pela psicóloga logada.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<List<SessionDTOs.View>> listMine() {
        return ResponseEntity.ok(sessionService.listMine());
    }

    @PostMapping
    @Operation(summary = "Cria uma sessão e notifica os alunos (mock de e-mail + convite .ics).")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<SessionDTOs.View> create(@Valid @RequestBody SessionDTOs.Insert dto) {
        return ResponseEntity.status(201).body(sessionService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita, remarca ou cancela uma sessão.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<SessionDTOs.View> update(@PathVariable Long id, @Valid @RequestBody SessionDTOs.Update dto) {
        return ResponseEntity.ok(sessionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma sessão.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------- aluno (RF-16) --------

    @GetMapping("/mine")
    @Operation(summary = "Lista as sessões do aluno logado.")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SessionDTOs.View>> forStudent() {
        return ResponseEntity.ok(sessionService.listForCurrentStudent());
    }

    // -------- convite iCalendar --------

    @GetMapping(value = "/{id}/ics", produces = "text/calendar")
    @Operation(summary = "Baixa o convite .ics da sessão (abre no Google Calendar/Outlook).")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN','USER')")
    public ResponseEntity<byte[]> ics(@PathVariable Long id) {
        byte[] body = sessionService.ics(id).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"sessao-" + id + ".ics\"")
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .body(body);
    }
}
