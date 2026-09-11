package com.synccarreira.synccarreira_api.services.validation;

/**
 * Validação de CPF e CNPJ (dígitos verificadores) — seção 7 do documento de requisitos.
 * Métodos estáticos, sem estado.
 */
public final class DocumentoValidator {

    private DocumentoValidator() {
    }

    /** Remove tudo que não for dígito. */
    public static String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean isCpfValido(String cpf) {
        String d = apenasDigitos(cpf);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (d.charAt(i) - '0') * (10 - i);
            }
            int dig1 = 11 - (soma % 11);
            if (dig1 >= 10) dig1 = 0;
            if (dig1 != d.charAt(9) - '0') return false;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (d.charAt(i) - '0') * (11 - i);
            }
            int dig2 = 11 - (soma % 11);
            if (dig2 >= 10) dig2 = 0;
            return dig2 == d.charAt(10) - '0';
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isCnpjValido(String cnpj) {
        String d = apenasDigitos(cnpj);
        if (d.length() != 14 || d.chars().distinct().count() == 1) {
            return false;
        }
        try {
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += (d.charAt(i) - '0') * peso1[i];
            }
            int dig1 = soma % 11 < 2 ? 0 : 11 - (soma % 11);
            if (dig1 != d.charAt(12) - '0') return false;

            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += (d.charAt(i) - '0') * peso2[i];
            }
            int dig2 = soma % 11 < 2 ? 0 : 11 - (soma % 11);
            return dig2 == d.charAt(13) - '0';
        } catch (RuntimeException e) {
            return false;
        }
    }
}
