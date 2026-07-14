package com.intern.employeeservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Service
public class SsnHashingService {

    private static final String ALGORITHM = "HmacSHA256";
    private final SecretKeySpec secretKeySpec;

    public SsnHashingService(@Value("${security.ssn-secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("SSN hashing secret must not be blank");
        }

        this.secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    public String hash(String socialSecurityNumber) {
        if (socialSecurityNumber == null || socialSecurityNumber.isBlank()) {
            throw new IllegalArgumentException("Social security number must not be blank");
        }


        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hashBytes = mac.doFinal(socialSecurityNumber.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not hash social security number", exception);
        }
    }


}
