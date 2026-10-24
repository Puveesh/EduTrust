package com.sentinel.ledger.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyCredentialRequest {
    @NotBlank(message = "Credential ID is required")
    private String credentialId;

    // Optional raw payload if performing direct cryptographic verification of signed data
    private String payloadJson;
    private String signature;
    private String publicKeyPem;

    public VerifyCredentialRequest() {}

    public VerifyCredentialRequest(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getPublicKeyPem() { return publicKeyPem; }
    public void setPublicKeyPem(String publicKeyPem) { this.publicKeyPem = publicKeyPem; }
}
