package com.attendance.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling password hashing and verification
 */
public class PasswordUtils {
    private static final Logger LOGGER = Logger.getLogger(PasswordUtils.class.getName());
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Generates a random salt for password hashing
     * @return Base64 encoded salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hashes a password with a given salt using SHA-256
     * @param password The plain text password
     * @param salt The salt value (Base64 encoded)
     * @return The hashed password (Base64 encoded)
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            
            // Update digest with salt first
            md.update(saltBytes);
            
            // Then with password bytes
            byte[] hashedBytes = md.digest(password.getBytes());
            
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Hashing algorithm not available", e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Verifies a password against a stored hash and salt
     * @param inputPassword The password to verify
     * @param storedHash The stored hash to compare against
     * @param storedSalt The stored salt used for hashing
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) {
        String hashedInput = hashPassword(inputPassword, storedSalt);
        return hashedInput.equals(storedHash);
    }
    
    /**
     * Convenience method to generate a complete password hash with salt
     * @param password The plain text password
     * @return A string containing the salt and hash, separated by a colon
     */
    public static String generateSecurePassword(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return salt + ":" + hash;
    }
    
    /**
     * Verifies a password against a complete stored hash (salt:hash)
     * @param inputPassword The password to verify
     * @param storedSecurePassword The stored secure password (salt:hash)
     * @return true if password matches, false otherwise
     */
    public static boolean verifySecurePassword(String inputPassword, String storedSecurePassword) {
        String[] parts = storedSecurePassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        String salt = parts[0];
        String hash = parts[1];
        return verifyPassword(inputPassword, hash, salt);
    }
}