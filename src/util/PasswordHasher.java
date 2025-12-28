package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final String HASH_ALGO = "SHA-256";

    public static String hash(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGO);
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[salt.length + hashed.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashed, 0, combined, salt.length, hashed.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash algorithm not found", e);
        }
    }

    public static boolean verify(String password, String stored) {
        try {
            byte[] decoded = Base64.getDecoder().decode(stored);
            if (decoded.length < 17) return false; // require at least 16-byte salt + 1 byte hash
            byte[] salt = new byte[16];
            System.arraycopy(decoded, 0, salt, 0, 16);
            String recomputed = hash(password, salt);
            return constantTimeEquals(stored, recomputed);
        } catch (IllegalArgumentException e) {
            // Not a Base64-encoded salted-hash we produce (e.g., bcrypt string). Treat as non-match.
            return false;
        }
    }

    public static String hashWithRandomSalt(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return hash(password, salt);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}


