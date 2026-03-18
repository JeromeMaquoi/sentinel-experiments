package be.unamur.snail.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

/**
 * Utility class for calculating checksums of files and content.
 */
public class ChecksumUtil {
    private ChecksumUtil() {}

    /**
     * Calculate SHA-256 checksum of a file.
     *
     * @param filePath Path to the file
     * @return Hex string of SHA-256 checksum
     * @throws Exception if file cannot be read or hashing fails
     */
    public static String calculateFileChecksum(Path filePath) throws Exception {
        byte[] fileContent = Files.readAllBytes(filePath);
        return calculateChecksum(fileContent, "SHA-256");
    }

    /**
     * Calculate SHA-256 checksum of content.
     *
     * @param content The content to hash
     * @return Hex string of SHA-256 checksum
     * @throws Exception if hashing fails
     */
    public static String calculateChecksum(String content) throws Exception {
        return calculateChecksum(content.getBytes(), "SHA-256");
    }

    /**
     * Calculate checksum of bytes using specified algorithm.
     *
     * @param data The data to hash
     * @param algorithm The algorithm (e.g., "SHA-256", "MD5")
     * @return Hex string of checksum
     * @throws Exception if hashing fails
     */
    public static String calculateChecksum(byte[] data, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hashBytes = digest.digest(data);
        return bytesToHex(hashBytes);
    }

    /**
     * Convert byte array to hexadecimal string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

