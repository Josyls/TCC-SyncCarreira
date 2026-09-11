package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.psicologa.PanelDTOs;
import com.synccarreira.synccarreira_api.services.PsychologistPanelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/psychologist-panel")
@Tag(name = "Painel da psicóloga", description = "Status por aluno (sem respostas individuais — RN-03), alertas automáticos (RF-04) e priorização (RF-05).")
@PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
public class PsychologistPanelController {

    @Autowired
    private PsychologistPanelService panelService;

    @GetMapping
    @Operation(summary = "Painel de status por aluno + alertas abertos. Filtro opcional só 'em dúvida' (RF-05).")
    public ResponseEntity<PanelDTOs.PanelSummary> panel(
            @RequestParam(required = false, name = "onlyInDoubt") Boolean onlyInDoubt) {
        return ResponseEntity.ok(panelService.panel(onlyInDoubt));
    }

    @PostMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "Marca um alerta de necessidade de orientação como resolvido.")
    public ResponseEntity<Void> resolveAlert(@PathVariable Long alertId) {
        panelService.resolveAlert(alertId);
        return ResponseEntity.noContent().build();
    }
}
