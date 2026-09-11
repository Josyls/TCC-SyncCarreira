package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.journey.*;
import com.synccarreira.synccarreira_api.services.JourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/journeys")
@Tag(name = "Jornada do aluno", description = "Trilhas em ordem, sínteses obrigatórias, panorama e histórico (RF-10, RF-11, RF-13, RF-15). Somente aluno.")
@PreAuthorize("hasRole('USER')")
public class JourneyController {

    @Autowired
    private JourneyService journeyService;

    @GetMapping("/current")
    @Operation(summary = "Retorna a jornada em andamento do aluno; inicia uma nova se não houver.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Jornada retornada com sucesso."))
    public ResponseEntity<JourneyViewDTO> current() {
        return ResponseEntity.ok(journeyService.currentOrStart());
    }

    @GetMapping("/{journeyId}")
    @Operation(summary = "Panorama de progresso de uma jornada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK."),
            @ApiResponse(responseCode = "403", description = "Jornada de outro aluno."),
            @ApiResponse(responseCode = "404", description = "Jornada não encontrada.")
    })
    public ResponseEntity<JourneyViewDTO> view(@PathVariable Long journeyId) {
        return ResponseEntity.ok(journeyService.view(journeyId));
    }

    @GetMapping("/{journeyId}/trails/{trailId}")
    @Operation(summary = "Detalhe de uma trilha para responder (só se liberada — RN-01).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK."),
            @ApiResponse(responseCode = "403", description = "Trilha bloqueada ou jornada de outro aluno.")
    })
    public ResponseEntity<TrailDetailDTO> trail(@PathVariable Long journeyId, @PathVariable Long trailId) {
        return ResponseEntity.ok(journeyService.trailDetail(journeyId, trailId));
    }

    @PutMapping("/{journeyId}/answers")
    @Operation(summary = "Salva ou atualiza a resposta de uma pergunta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resposta salva."),
            @ApiResponse(responseCode = "403", description = "Trilha bloqueada (RN-01)."),
            @ApiResponse(responseCode = "422", description = "Dados inválidos.")
    })
    public ResponseEntity<JourneyViewDTO> answer(@PathVariable Long journeyId,
                                                 @Valid @RequestBody JourneyRequests.SaveAnswer req) {
        return ResponseEntity.ok(journeyService.saveAnswer(journeyId, req));
    }

    @PutMapping("/{journeyId}/trails/{trailId}/synthesis")
    @Operation(summary = "Registra a síntese textual obrigatória da trilha; conclui a trilha e libera a próxima (RF-11, RN-01, RN-02).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Síntese registrada / trilha concluída."),
            @ApiResponse(responseCode = "403", description = "Trilha bloqueada."),
            @ApiResponse(responseCode = "422", description = "Perguntas incompletas ou síntese vazia.")
    })
    public ResponseEntity<JourneyViewDTO> synthesis(@PathVariable Long journeyId, @PathVariable Long trailId,
                                                    @Valid @RequestBody JourneyRequests.SaveSynthesis req) {
        return ResponseEntity.ok(journeyService.saveTrailSynthesis(journeyId, trailId, req));
    }

    @GetMapping("/{journeyId}/final")
    @Operation(summary = "Etapa final de Síntese: consolida as sínteses e apresenta o leque de carreiras (RF-12).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK."),
            @ApiResponse(responseCode = "403", description = "Ainda há trilhas não concluídas (RN-01).")
    })
    public ResponseEntity<FinalPhaseDTO> finalPhase(@PathVariable Long journeyId) {
        return ResponseEntity.ok(journeyService.finalPhase(journeyId));
    }

    @PostMapping("/{journeyId}/complete")
    @Operation(summary = "Conclui a jornada com a síntese final e a sinalização de dúvida (RF-04, RN-04).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jornada concluída."),
            @ApiResponse(responseCode = "403", description = "Trilhas não concluídas."),
            @ApiResponse(responseCode = "422", description = "Síntese final vazia ou sinalização ausente.")
    })
    public ResponseEntity<FinalPhaseDTO> complete(@PathVariable Long journeyId,
                                                  @Valid @RequestBody JourneyRequests.CompleteJourney req) {
        return ResponseEntity.ok(journeyService.completeJourney(journeyId, req));
    }

    @GetMapping("/{journeyId}/panorama")
    @Operation(summary = "Histórico completo de respostas e sínteses da jornada (RF-13, RN-08).")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK."))
    public ResponseEntity<PanoramaDTO> panorama(@PathVariable Long journeyId) {
        return ResponseEntity.ok(journeyService.panorama(journeyId));
    }

    @GetMapping("/history")
    @Operation(summary = "Jornadas de ciclos anteriores do aluno (RF-15).")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "OK."))
    public ResponseEntity<List<JourneyHistoryItemDTO>> history() {
        return ResponseEntity.ok(journeyService.myHistory());
    }
}
