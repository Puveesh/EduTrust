package com.sentinel.ledger;

import com.sentinel.ledger.service.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService("test-key-id", "SHA256withRSA");
    }

    @Test
    @DisplayName("Should successfully sign and verify genuine credential payload")
    void testSignAndVerifySuccess() {
        String data = "{\"credentialId\":\"AICTE-CRED-12345\",\"syntheticSubjectRef\":\"APAAR-SYN-2024-1001\"}";
        String signature = cryptoService.sign(data);
        String pubKey = cryptoService.getPublicKeyPem();

        assertNotNull(signature);
        assertNotNull(pubKey);

        boolean verified = cryptoService.verify(data, signature, pubKey);
        assertTrue(verified, "Digital signature must verify successfully for untampered data");
    }

    @Test
    @DisplayName("Should detect tampering when signed payload is modified")
    void testDetectTampering() {
        String originalData = "{\"qualification\":\"B.Tech Computer Science\",\"cgpa\":\"8.5\"}";
        String signature = cryptoService.sign(originalData);
        String pubKey = cryptoService.getPublicKeyPem();

        // Tamper with data (e.g. modify CGPA to 9.9)
        String tamperedData = "{\"qualification\":\"B.Tech Computer Science\",\"cgpa\":\"9.9\"}";

        boolean verified = cryptoService.verify(tamperedData, signature, pubKey);
        assertFalse(verified, "Digital signature check must FAIL when payload is modified");
    }

    @Test
    @DisplayName("Should fail verification when signature is corrupted")
    void testCorruptedSignature() {
        String data = "{\"credentialId\":\"AICTE-CRED-99999\"}";
        String signature = cryptoService.sign(data);
        String pubKey = cryptoService.getPublicKeyPem();

        String corruptedSig = signature.substring(0, signature.length() - 4) + "AAAA";
        boolean verified = cryptoService.verify(data, corruptedSig, pubKey);
        assertFalse(verified, "Verification must fail with corrupted signature");
    }
}
