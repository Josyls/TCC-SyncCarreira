package com.synccarreira.synccarreira_api.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Converte Strings sensíveis para texto cifrado (AES-256/GCM) na persistência e
 * decifra na leitura. Atende ao RNF-02 (dados sensíveis armazenados com criptografia).
 *
 * <p>Aplicado apenas em campos de entidades NOVAS (sínteses, perfil do aluno,
 * feedback, respostas da nova jornada). O armazenamento legado
 * ({@code tb_respostas_aluno.conteudo}) permanece em texto claro por não ser
 * possível alterar o código legado que o grava.</p>
 *
 * <p>A chave vem da variável de ambiente {@code SC_CRYPTO_KEY} (Base64). Sem ela,
 * usa uma chave de desenvolvimento fixa — troque em produção.</p>
 */
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final String PREFIX = "enc:v1:";

    private static final String DEV_DEFAULT_KEY =
            "c3luY2NhcnJlaXJhLWRldi1zZWNyZXQta2V5LTMyLWJ5dGVzIQ==";

    private static final SecretKey KEY = resolveKey();
    private static final SecureRandom RANDOM = new SecureRandom();

    private static SecretKey resolveKey() {
        String base64 = System.getenv("SC_CRYPTO_KEY");
        if (base64 == null || base64.isBlank()) {
            base64 = DEV_DEFAULT_KEY;
        }
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException e) {
            raw = base64.getBytes(StandardCharsets.UTF_8);
        }
        // Normaliza para exatamente 32 bytes (AES-256) via SHA-256.
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw);
            return new SecretKeySpec(digest, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível derivar a chave de criptografia", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return PREFIX + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao cifrar dado sensível", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (!dbData.startsWith(PREFIX)) {
            // Compatibilidade: valor gravado antes da criptografia entrar em vigor.
            return dbData;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbData.substring(PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH_BYTES);
            byte[] cipherText = Arrays.copyOfRange(combined, IV_LENGTH_BYTES, combined.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao decifrar dado sensível", e);
        }
    }
}
