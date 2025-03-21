package com.attendance.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 */
public class PasswordHashing {
    
    private static final int SALT_LENGTH = 16;
    
    /**
     * Generates a random salt
     * @return Base64 encoded salt string
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hashes a password with a given salt using SHA-256
     * @param password The password to hash
     * @param salt The salt to use
     * @return Base64 encoded hash
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify if a password matches a stored hash
     * @param password The password to check
     * @param storedSalt The stored salt
     * @param storedHash The stored hash
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedSalt, String storedHash) {
        String hashedPassword = hashPassword(password, storedSalt);
        return hashedPassword.equals(storedHash);
    }
    
    /**
     * Creates a combined hash and salt string for storage
     * @param password The password to hash
     * @return A string in the format "hash:salt"
     */
    public static String createPasswordHash(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return hash + ":" + salt;
    }
    
    /**
     * Verifies a password against a combined hash:salt string
     * @param password The password to verify
     * @param storedValue The stored "hash:salt" value
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedValue) {
        String[] parts = storedValue.split(":");
        if (parts.length != 2) {
            return false;
        }
        String storedHash = parts[0];
        String storedSalt = parts[1];
        return verifyPassword(password, storedSalt, storedHash);
    }
}
