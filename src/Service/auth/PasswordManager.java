/**
 *
 * @author Leianna Cruz
 */

package service.auth;

import java.io.IOException;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class PasswordManager {

    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int HASH_ITERATIONS = 65536;
    private static final int HASH_BITS = 256;
    private static final int SALT_BYTES = 16;

    private final SecureRandom secureRandom;

    public PasswordManager() {
        this.secureRandom = new SecureRandom();
    }

    public byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        return salt;
    }
    
    public byte[] hashPassword(char[] password, byte[] salt) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_ALGORITHM);
            KeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_BITS);
            return skf.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to hash password.", ex);
        }
    }

    public boolean verifyPassword(char[] inputPassword, byte[] salt, byte[] hash) throws IOException {
        if (inputPassword == null || salt == null || hash == null) {
            return false;
        }
        //byte[] computedHash = hashPassword(inputPassword, salt);
        //return constantTimeEquals(computedHash, hash);
        
        byte[] computedHash = hashPassword(inputPassword, salt);

        boolean match = constantTimeEquals(computedHash, hash);
        return match;
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

}