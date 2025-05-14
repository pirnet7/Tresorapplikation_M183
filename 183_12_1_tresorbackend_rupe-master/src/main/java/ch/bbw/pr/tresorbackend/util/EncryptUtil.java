package ch.bbw.pr.tresorbackend.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * EncryptUtil
 * Used to encrypt and decrypt content using AES/GCM/NoPadding.
 * Derives an encryption key from a password and salt using PBKDF2WithHmacSHA256.
 *
 * @author Peter Rutschmann (Original structure)
 * @author AI Assistant (Implementation)
 */
public class EncryptUtil {

    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int ITERATION_COUNT = 65536; // Standard iteration count for PBKDF2
    private static final int KEY_LENGTH_BITS = 256; // For AES-256
    private static final int GCM_IV_LENGTH_BYTES = 12; // Recommended IV length for GCM is 12 bytes (96 bits)
    private static final int GCM_TAG_LENGTH_BITS = 128; // GCM authentication tag length

    private SecretKey derivedKey;

    public EncryptUtil(String password, String saltHex) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = hexStringToByteArray(saltHex);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS);
        SecretKey tmp = factory.generateSecret(spec);
        this.derivedKey = new SecretKeySpec(tmp.getEncoded(), ENCRYPTION_ALGORITHM);
    }

    public String encrypt(String data) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv); // Generate a random IV

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, this.derivedKey, gcmParameterSpec);

        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Prepend IV to ciphertext and Base64 encode
        // IV (12 bytes) + Ciphertext
        byte[] ivAndCiphertext = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.length);
        System.arraycopy(encryptedData, 0, ivAndCiphertext, iv.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(ivAndCiphertext);
    }

    public String decrypt(String encryptedDataWithIv) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedDataWithIv);

        // Extract IV from the beginning of the decoded data
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        System.arraycopy(decodedData, 0, iv, 0, iv.length);

        byte[] encryptedActualData = new byte[decodedData.length - iv.length];
        System.arraycopy(decodedData, iv.length, encryptedActualData, 0, encryptedActualData.length);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, this.derivedKey, gcmParameterSpec);

        byte[] decryptedData = cipher.doFinal(encryptedActualData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    // Helper to convert hex string salt to byte array
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
