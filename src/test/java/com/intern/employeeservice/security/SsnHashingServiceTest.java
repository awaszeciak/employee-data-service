package com.intern.employeeservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SsnHashingServiceTest {

    private SsnHashingService hashingService;

    @BeforeEach
    void setUp() {
        hashingService = new SsnHashingService("test-secret-key");
    }

    @Test
    void shouldGenerateSameHashForSameSsn() {
        String firstHash = hashingService.hash("123456789");
        String secondHash = hashingService.hash("123456789");

        assertEquals(firstHash,secondHash);
    }

    @Test
    void shouldGenerateDifferentHashForDifferentSsn() {
        String firstHash = hashingService.hash("123456789");
        String secondHash = hashingService.hash("123456781");

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void shouldNotReturnPlaintextSsn() {
        String ssn = "123456789";
        String hash = hashingService.hash(ssn);

        assertNotEquals(ssn, hash);
        assertFalse(hash.contains(ssn));
    }

    @Test
    void shouldThrowExceptionForBlankSsn() {
        assertThrows(IllegalArgumentException.class,
                () -> hashingService.hash(" "));
    }

    @Test
    void shouldThrowExceptionForNullSsn() {
        assertThrows(IllegalArgumentException.class,
                () -> hashingService.hash(null));
    }

    @Test
    void shouldReturnBase64EncodedSha256Hash() {
        String hash = hashingService.hash("123456789");

        assertEquals(44, hash.length());
    }

    @Test
    void shouldThrowExceptionWhenSecretIsNull() {
        assertThrows(IllegalStateException.class,
                () -> new SsnHashingService(null));
    }

    @Test
    void shouldThrowExceptionWhenSecretIsBlank() {
        assertThrows(IllegalStateException.class,
                () -> new SsnHashingService(" "));
    }

}
