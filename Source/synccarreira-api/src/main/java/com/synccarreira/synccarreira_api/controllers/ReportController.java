package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.services.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/reports")
@Tag(name = "Relatórios", description = "Relatórios de turma e individuais em CSV (RF-07). O de turma não expõe respostas (RN-03).")
@PreAuthorize("hasAnyRole('PSICOLOGA','ADMIN')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping(value = "/class", produces = "text/csv")
    @Operation(summary = "Relatório geral da turma (CSV). Filtro opcional por turma.")
    public ResponseEntity<byte[]> classReport(@RequestParam(required = false) Long classId) {
        return csv(reportService.classReportCsv(classId), "relatorio-turma.csv");
    }

    @GetMapping(value = "/student/{studentId}", produces = "text/csv")
    @Operation(summary = "Relatório individual do aluno com sínteses e status (CSV).")
    public ResponseEntity<byte[]> studentReport(@PathVariable Long studentId) {
        return csv(reportService.individualReportCsv(studentId), "relatorio-aluno-" + studentId + ".csv");
    }

    private ResponseEntity<byte[]> csv(String content, String filename) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(content.getBytes(StandardCharsets.UTF_8));
    }
}
