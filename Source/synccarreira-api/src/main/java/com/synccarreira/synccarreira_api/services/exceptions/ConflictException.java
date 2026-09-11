package com.synccarreira.synccarreira_api.services.exceptions;

/** Violação de unicidade ou estado conflitante (HTTP 409). Ex.: CPF/CNPJ já cadastrado. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
