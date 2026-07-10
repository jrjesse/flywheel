package com.antigravity.sales.security.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AesGcmCryptoServiceTest {

    @Test
    void encryptDecryptRoundTrip() {
        AesGcmCryptoService crypto = new AesGcmCryptoService("0123456789abcdef0123456789abcdef");
        String plaintext = "sk_live_super_secret_token";
        String encrypted = crypto.encrypt(plaintext);
        assertNotEquals(plaintext, encrypted);
        assertEquals(plaintext, crypto.decrypt(encrypted));
    }

    @Test
    void samePlaintextProducesDifferentCiphertext() {
        AesGcmCryptoService crypto = new AesGcmCryptoService("0123456789abcdef0123456789abcdef");
        String a = crypto.encrypt("token");
        String b = crypto.encrypt("token");
        assertNotEquals(a, b);
    }
}
