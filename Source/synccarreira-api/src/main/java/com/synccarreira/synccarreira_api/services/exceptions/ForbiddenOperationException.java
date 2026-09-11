package com.synccarreira.synccarreira_api.services.exceptions;

/**
 * Operação negada por regra de autorização de negócio (HTTP 403).
 * Ex.: psicóloga tentando acessar aluno de outra instituição (RNF-08).
 */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
