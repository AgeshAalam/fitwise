package com.fitwise.encryption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class AESEncryption {


    @Value("${aes.encryption.secret}")
    private String secret;

    @Value("${aes.encryption.salt}")
    private String salt;

    @Value("${aes.encryption.initialization.vector}")
    private String initializationVector;


    /**
     * Generate secret key for encryption
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private SecretKey getSecretKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secretKey;
    }

    /**
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    private IvParameterSpec generateIv() throws UnsupportedEncodingException {
        IvParameterSpec iv = new IvParameterSpec(initializationVector.getBytes("UTF-8"));
        return iv;
    }

    /**
     * Encrypting a string
     * @param input
     * @param key
     * @param iv
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private String encrypt(String input, SecretKey key,
                           IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder()
                .encodeToString(cipherText);
    }

    /**
     * Decrypting a string
     * @param cipherText
     * @param key
     * @param iv
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private String decrypt(String cipherText, SecretKey key,
                           IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(cipherText));
        return new String(plainText);
    }


    /**
     * Secret key, IV generation and encryption
     * @param input
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     */
    public String encrypt(String input) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException {
        long start = new Date().getTime();
        SecretKey secretKey = getSecretKey();
        log.info("Secret key generation : Time taken in millis : "+(new Date().getTime()-start));
        start = new Date().getTime();
        IvParameterSpec ivParameterSpec = generateIv();
        log.info("IV generation : Time taken in millis : "+(new Date().getTime()-start));
        start = new Date().getTime();
        String encryptedString = encrypt(input, secretKey, ivParameterSpec);
        log.info("Encryption : Time taken in millis : "+(new Date().getTime()-start));
        return encryptedString;
    }

    /**
     * Secret key, IV generation and encryption
     * @param encryptedString
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     */
    public String decrypt(String encryptedString) throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException {
        long start = new Date().getTime();
        SecretKey secretKey = getSecretKey();
        log.info("Secret key generation : Time taken in millis : "+(new Date().getTime()-start));
        start = new Date().getTime();
        IvParameterSpec ivParameterSpec = generateIv();
        log.info("IV generation : Time taken in millis : "+(new Date().getTime()-start));
        start = new Date().getTime();
        String decryptedString = decrypt(encryptedString, secretKey, ivParameterSpec);
        log.info("Decryption : Time taken in millis : "+(new Date().getTime()-start));

        return decryptedString;
    }
}
