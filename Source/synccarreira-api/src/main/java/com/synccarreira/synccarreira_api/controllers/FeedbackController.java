package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.psicologa.FeedbackDTOs;
import com.synccarreira.synccarreira_api.services.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedbacks")
@Tag(name = "Feedbacks", description = "Feedback textual da psicóloga para o aluno (RF-09); leitura pelo aluno (RF-16).")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Envia um feedback textual para um aluno.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<FeedbackDTOs.View> create(@Valid @RequestBody FeedbackDTOs.Insert dto) {
        return ResponseEntity.status(201).body(feedbackService.create(dto));
    }

    @GetMapping("/mine")
    @Operation(summary = "Feedbacks recebidos pelo aluno logado.")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FeedbackDTOs.View>> mine() {
        return ResponseEntity.ok(feedbackService.listMineAsStudent());
    }

    @GetMapping("/sent")
    @Operation(summary = "Feedbacks enviados pela psicóloga logada.")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<List<FeedbackDTOs.View>> sent() {
        return ResponseEntity.ok(feedbackService.listSentByPsychologist());
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Feedbacks de um aluno específico (psicóloga).")
    @PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
    public ResponseEntity<List<FeedbackDTOs.View>> forStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(feedbackService.listForStudent(studentId));
    }
}
