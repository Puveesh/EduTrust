package com.sentinel.ledger.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class CryptoService {

    private final KeyPair keyPair;
    private final String keyId;
    private final String algorithm;

    public CryptoService(
            @Value("${app.signing.key-id:aicte-credential-signer-v1}") String keyId,
            @Value("${app.signing.algorithm:SHA256withRSA}") String algorithm) {
        this.keyId = keyId;
        this.algorithm = algorithm;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.keyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize RSA key generator", e);
        }
    }

    public String getKeyId() {
        return keyId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getPublicKeyPem() {
        byte[] encoded = keyPair.getPublic().getEncoded();
        String base64 = Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN PUBLIC KEY-----\n" +
                base64.replaceAll("(.{64})", "$1\n") +
                "\n-----END PUBLIC KEY-----";
    }

    public String sign(String data) {
        try {
            Signature rsa = Signature.getInstance(algorithm);
            rsa.initSign(keyPair.getPrivate());
            rsa.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signature = rsa.sign();
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Error generating digital signature", e);
        }
    }

    public boolean verify(String data, String signatureBase64, String publicKeyPem) {
        try {
            String cleanKey = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(spec);

            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
}
