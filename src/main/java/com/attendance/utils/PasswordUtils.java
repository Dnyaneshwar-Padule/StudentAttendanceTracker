package com.attendance.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for password hashing and verification
 */
public class PasswordUtils {
    private static final Logger LOGGER = Logger.getLogger(PasswordUtils.class.getName());
    
    // Constants for salt generation
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Generate a secure random salt
     * 
     * @return the generated salt as a Base64 encoded string
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash a password with the provided salt using SHA-256
     * 
     * @param password the password to hash
     * @param salt the salt to use
     * @return the hashed password
     */
    public static String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(saltBytes);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Error hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a hashed password and salt
     * 
     * @param password the password to verify
     * @param hashedPassword the stored hashed password
     * @param salt the salt used for hashing
     * @return true if the password matches
     */
    public static boolean verifyPassword(String password, String hashedPassword, String salt) {
        String hashedInput = hashPassword(password, salt);
        return hashedInput.equals(hashedPassword);
    }
    
    /**
     * For development/testing only - verify a password without using salt/hash
     * This should NOT be used in production
     * 
     * @param inputPassword the password to verify
     * @param storedPassword the stored password (in plain text)
     * @return true if the password matches
     */
    public static boolean verifyPlainTextPassword(String inputPassword, String storedPassword) {
        return inputPassword.equals(storedPassword);
    }
    
    /**
     * Generate a secure password hash (with auto-generated salt)
     * This is a simplified version that generates salt internally and returns a formatted string
     * 
     * @param password the password to hash
     * @return a formatted string containing both the salt and hash, separated by ':'
     */
    public static String generateSecurePassword(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return salt + ":" + hash;
    }
    
    /**
     * Verify a password against a secure formatted password string (salt:hash)
     * 
     * @param password the password to verify
     * @param securePassword the stored secure password string (salt:hash)
     * @return true if the password matches
     */
    public static boolean verifySecurePassword(String password, String securePassword) {
        String[] parts = securePassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        String salt = parts[0];
        String hash = parts[1];
        
        return verifyPassword(password, hash, salt);
    }
}