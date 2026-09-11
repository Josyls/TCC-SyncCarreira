package com.synccarreira.synccarreira_api.controllers.handlers;

import com.synccarreira.synccarreira_api.dto.CustomError;
import com.synccarreira.synccarreira_api.services.exceptions.BusinessException;
import com.synccarreira.synccarreira_api.services.exceptions.ConflictException;
import com.synccarreira.synccarreira_api.services.exceptions.ForbiddenOperationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

/**
 * Tratamento de exceções das funcionalidades novas (Blocos 1–6).
 * Mantém o mesmo formato de corpo de erro do handler legado
 * ({@code {timestamp, status, error, path}}) para o frontend não precisar
 * de código de parsing diferente.
 *
 * <p>Ordenado antes do {@code ControllerExceptionHandler} legado para que os
 * tipos novos tenham precedência; os tipos legados continuam caindo no handler
 * original.</p>
 */
@ControllerAdvice
@Order(0)
public class ControllerExceptionHandlerV2 {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<CustomError> conflict(ConflictException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CustomError> business(BusinessException e, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<CustomError> forbiddenOperation(ForbiddenOperationException e, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, e.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomError> accessDenied(AccessDeniedException e, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado: seu perfil não permite esta operação.", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomError> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage(), request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CustomError> entityNotFound(EntityNotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    private ResponseEntity<CustomError> build(HttpStatus status, String message, HttpServletRequest request) {
        CustomError err = new CustomError(Instant.now(), status.value(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }
}
