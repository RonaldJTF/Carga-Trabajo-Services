package co.edu.unipamplona.ciadti.cargatrabajo.services.config.cipher;

import co.edu.unipamplona.ciadti.cargatrabajo.services.util.Trace;
import com.fasterxml.uuid.Generators;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;

public class AES {
    private final int keySize;
    private final int iterationCount;
    private final Cipher cipher;

    public AES(int keySize, int iterationCount) {
        this.keySize = keySize;
        this.iterationCount = iterationCount;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw fail(e);
        }
    }

    public String decrypt(String salt, String iv, String passphrase, String ciphertext) {
        try {
            SecretKey key = generateKey(salt, passphrase);
            byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, base64(ciphertext));
            return new String(decrypted, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] doFinal(int encryptMode, SecretKey key, String iv, byte[] bytes) {
        try {
            cipher.init(encryptMode, key, new IvParameterSpec(hex(iv)));
            return cipher.doFinal(bytes);
        } catch (InvalidKeyException
                 | InvalidAlgorithmParameterException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {
            Trace.logError(e);
            return null;
        }
    }

    public static byte[] base64(String str) {
        return Base64.decodeBase64(str);
    }

    public static byte[] hex(String str) {
        try {
            return Hex.decodeHex(str.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalStateException(e);
        }
    }

    private IllegalStateException fail(Exception e) {
        return null;
    }

    /**
     * Leonel Pérez - 02 de octubre 2024
     *
     * @param value
     * @return
     */

    private SecretKey generateKey(String salt, String passphrase) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), iterationCount, keySize);
            SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return key;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    private String generateSalt(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(value.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    String decryptParam(String encrypted) {
        String[] parts = encrypted.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("El formato es incorrecto");
        }
        String uuid = parts[0];
        String encodedEncrypted = parts[1];
        String salt = generateSalt(uuid);
        String iv = Generators.nameBasedGenerator(UUID.fromString(uuid)).generate(salt).toString().replace("-", "");
        return decrypt(salt, iv, uuid, encodedEncrypted);
    }

    String encryptParam(String message) {
        try {
            String uuid = UUID.randomUUID().toString();
            String salt = generateSalt(uuid);
            SecretKey key = generateKey(salt, uuid);
            String iv = Generators.nameBasedGenerator(UUID.fromString(uuid)).generate(salt).toString().replace("-", "");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(hex(iv)));
            byte[] encrypted = cipher.doFinal(message.getBytes());
            return Base64.encodeBase64String((uuid + "|" + Base64.encodeBase64String(encrypted)).getBytes());
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            Trace.logError(e);
            return e.getMessage();
        }
    }

}