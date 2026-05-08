/**
 *
 * @author Leianna Cruz
 */

package service.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class PasswordManager {

    private static final String APP_DIR_NAME = ".motorph";
    private static final String CREDENTIALS_FILE_NAME = "credentials.enc";
    private static final String KEY_FILE_NAME = "master.key";
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int HASH_ITERATIONS = 65536;
    private static final int HASH_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final int AES_KEY_BYTES = 32;
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final Path appDir;
    private final Path credentialsPath;
    private final Path keyPath;
    private final SecureRandom secureRandom;

    public PasswordManager() {
        String userHome = System.getProperty("user.home", ".");
        this.appDir = Paths.get(userHome, APP_DIR_NAME);
        this.credentialsPath = appDir.resolve(CREDENTIALS_FILE_NAME);
        this.keyPath = appDir.resolve(KEY_FILE_NAME);
        this.secureRandom = new SecureRandom();
    }

    public synchronized boolean hasAccounts() throws IOException {
        return !loadRecords().isEmpty();
    }

    public synchronized void upsertAccount(String username, char[] password) throws IOException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password is required.");
        }

        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = hashPassword(password, salt);

        Map<String, CredentialRecord> records = loadRecords();
        records.put(username.trim(), new CredentialRecord(salt, hash));
        saveRecords(records);
    }

    public synchronized boolean validate(String username, char[] password) throws IOException {
        if (username == null || username.trim().isEmpty() || password == null) {
            return false;
        }

        Map<String, CredentialRecord> records = loadRecords();
        CredentialRecord record = records.get(username.trim());
        if (record == null) {
            return false;
        }

        byte[] computedHash = hashPassword(password, record.salt);
        return constantTimeEquals(computedHash, record.hash);
    }

    public synchronized boolean changePassword(String username, char[] oldPassword, char[] newPassword) throws IOException {
        if (username == null || username.trim().isEmpty() ||
                oldPassword == null || newPassword == null || newPassword.length == 0) {
            return false;
        }

        Map<String, CredentialRecord> records = loadRecords();
        CredentialRecord record = records.get(username.trim());
        if (record == null) {
            return false;
        }

        byte[] oldHash = hashPassword(oldPassword, record.salt);
        if (!constantTimeEquals(oldHash, record.hash)) {
            return false;
        }

        byte[] newSalt = new byte[SALT_BYTES];
        secureRandom.nextBytes(newSalt);
        byte[] newHash = hashPassword(newPassword, newSalt);
        records.put(username.trim(), new CredentialRecord(newSalt, newHash));
        saveRecords(records);
        return true;
    }

    private Map<String, CredentialRecord> loadRecords() throws IOException {
        ensureAppDir();

        if (!Files.exists(credentialsPath)) {
            return new HashMap<>();
        }

        String encryptedContent = Files.readString(credentialsPath, StandardCharsets.UTF_8).trim();
        if (encryptedContent.isEmpty()) {
            return new HashMap<>();
        }

        String decrypted = decrypt(encryptedContent);
        Map<String, CredentialRecord> records = new HashMap<>();

        String[] lines = decrypted.split("\\R");
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }

            String[] parts = line.split(":");
            if (parts.length != 3) {
                continue;
            }

            String username = parts[0].trim();
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hash = Base64.getDecoder().decode(parts[2]);
            records.put(username, new CredentialRecord(salt, hash));
        }

        return records;
    }

    private void saveRecords(Map<String, CredentialRecord> records) throws IOException {
        ensureAppDir();

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, CredentialRecord> entry : records.entrySet()) {
            builder.append(entry.getKey())
                    .append(':')
                    .append(Base64.getEncoder().encodeToString(entry.getValue().salt))
                    .append(':')
                    .append(Base64.getEncoder().encodeToString(entry.getValue().hash))
                    .append(System.lineSeparator());
        }

        String encrypted = encrypt(builder.toString());
        Files.writeString(credentialsPath, encrypted, StandardCharsets.UTF_8);
    }

    private void ensureAppDir() throws IOException {
        if (!Files.exists(appDir)) {
            Files.createDirectories(appDir);
        }
    }

    private SecretKey loadOrCreateAesKey() throws IOException {
        ensureAppDir();

        if (Files.exists(keyPath)) {
            String stored = Files.readString(keyPath, StandardCharsets.UTF_8).trim();
            byte[] keyBytes = Base64.getDecoder().decode(stored);
            return new SecretKeySpec(keyBytes, "AES");
        }

        byte[] keyBytes = new byte[AES_KEY_BYTES];
        secureRandom.nextBytes(keyBytes);

        String encoded = Base64.getEncoder().encodeToString(keyBytes);
        Files.writeString(keyPath, encoded, StandardCharsets.UTF_8);

        return new SecretKeySpec(keyBytes, "AES");
    }

    private String encrypt(String plainText) throws IOException {
        try {
            SecretKey key = loadOrCreateAesKey();
            byte[] iv = new byte[GCM_IV_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            String ivPart = Base64.getEncoder().encodeToString(iv);
            String dataPart = Base64.getEncoder().encodeToString(encrypted);
            return ivPart + ":" + dataPart;
        } catch (GeneralSecurityException ex) {
            throw new IOException("Failed to encrypt credentials.", ex);
        }
    }

    private String decrypt(String encryptedContent) throws IOException {
        String[] parts = encryptedContent.split(":");
        if (parts.length != 2) {
            throw new IOException("Invalid credentials file format.");
        }

        try {
            SecretKey key = loadOrCreateAesKey();
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new IOException("Failed to decrypt credentials.", ex);
        }
    }

    private byte[] hashPassword(char[] password, byte[] salt) throws IOException {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_BITS);
            return skf.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IOException("Failed to hash password.", ex);
        }
    }

    private boolean constantTimeEquals(byte[] left, byte[] right) {
        if (left == null || right == null || left.length != right.length) {
            return false;
        }

        int diff = 0;
        for (int i = 0; i < left.length; i++) {
            diff |= left[i] ^ right[i];
        }
        return diff == 0;
    }

    private static class CredentialRecord {
        private final byte[] salt;
        private final byte[] hash;

        private CredentialRecord(byte[] salt, byte[] hash) {
            this.salt = salt;
            this.hash = hash;
        }
    }
}