package com.synccarreira.synccarreira_api.controllers;

import com.synccarreira.synccarreira_api.dto.*;
import com.synccarreira.synccarreira_api.services.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/students")
@Tag(name = "Alunos", description = "Endpoints para interagir com os alunos da aplicação.")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Operation(summary = "Busca todos os alunos cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Alunos encontrados com sucesso."
            )
    })
    @GetMapping
    public ResponseEntity<Page<StudentDTO>> findAll(Pageable pageable) {
        Page<StudentDTO> dto = studentService.findAll(pageable);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Busca um aluno cadastrado com base no id.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Aluno encontrado com sucesso.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class)) }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)) }
            )
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<StudentDTO> findById(@PathVariable Long id) {
        StudentDTO dto = studentService.findById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Insere um novo aluno no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Aluno inserido com sucesso.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class)) }
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Dados inválidos.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class)) }
            )
    })
    @PostMapping
    public ResponseEntity<StudentDTO> insert(@Valid @RequestBody StudentInsertDTO dto) {
        StudentDTO result = studentService.insert(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(uri).body(result);
    }

    @Operation(summary = "Atualiza um aluno já cadastrado no banco de dados com base no id.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Aluno atualizado com sucesso.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDTO.class)) }
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Dados inválidos.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationError.class)) }
            )
    })
    @PutMapping(value = "/{id}")
    public ResponseEntity<StudentDTO> update(
            @Parameter(description = "Id do aluno.", required = true)
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateDTO dto
    ) {
        StudentDTO result = studentService.update(id, dto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Deleta um aluno com base no id.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Aluno deletado com sucesso.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)) }
            )
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id do aluno.", required = true)
            @PathVariable Long id
    ) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/score")
    @Operation(summary = "Retorna o score acumulado do aluno por área do conhecimento.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Score encontrado com sucesso.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StudentScoreDTO.class)) }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Aluno não encontrado.",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)) }
            )
    })
    public ResponseEntity<StudentScoreDTO> getScore(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getScore(id));
    }
}
