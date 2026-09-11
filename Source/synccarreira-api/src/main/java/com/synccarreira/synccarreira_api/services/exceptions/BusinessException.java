package com.synccarreira.synccarreira_api.services.exceptions;

/** Regra de negócio violada com dados sintaticamente válidos (HTTP 422). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
