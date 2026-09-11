package com.synccarreira.synccarreira_api.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedStringConverterTest {

    private final EncryptedStringConverter converter = new EncryptedStringConverter();

    @Test
    void roundTrip_preservaOTextoOriginal() {
        String original = "Reflexão sensível do aluno — com acentos, çedilha e emojis 🙂";

        String cipher = converter.convertToDatabaseColumn(original);

        assertNotNull(cipher);
        assertTrue(cipher.startsWith("enc:v1:"), "deve ter prefixo de versão");
        assertNotEquals(original, cipher, "não pode vazar o texto claro");
        assertEquals(original, converter.convertToEntityAttribute(cipher));
    }

    @Test
    void doisEnc_produzemCiphersDiferentes_masMesmoPlaintext() {
        String original = "mesmo conteúdo";

        String a = converter.convertToDatabaseColumn(original);
        String b = converter.convertToDatabaseColumn(original);

        assertNotEquals(a, b, "IV aleatório deve produzir cipher diferente");
        assertEquals(original, converter.convertToEntityAttribute(a));
        assertEquals(original, converter.convertToEntityAttribute(b));
    }

    @Test
    void null_passaDireto() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void valorLegadoSemPrefixo_ehRetornadoComoEsta() {
        assertEquals("texto antigo em claro", converter.convertToEntityAttribute("texto antigo em claro"));
    }
}
