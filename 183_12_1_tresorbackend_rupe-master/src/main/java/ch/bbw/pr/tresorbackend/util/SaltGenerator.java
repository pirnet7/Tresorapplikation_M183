package ch.bbw.pr.tresorbackend.util;

import java.security.SecureRandom;
import java.util.HexFormat;

public class SaltGenerator {

    /**
     * Generates a cryptographically secure random salt.
     * @param numBytes The number of bytes for the salt (e.g., 16 for a 128-bit salt).
     * @return The salt encoded as a hexadecimal string.
     */
    public static String generateSaltHex(int numBytes) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[numBytes];
        random.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }
} 