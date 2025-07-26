package net.biswajit.journalApp.utils;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

@Component
public class CryptoUtil {
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private SecretKey key;

    @Value("${crypto.masterKey}")
    private String base64Key;

    @PostConstruct
    public void init() {
        byte[] raw = Base64.getDecoder().decode(base64Key);
        key = new SecretKeySpec(raw, "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = SecureRandom.getInstanceStrong().generateSeed(IV_BYTES);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] enc = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer bb = ByteBuffer.allocate(iv.length + enc.length);
            bb.put(iv); bb.put(enc);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception ex) {
            throw new IllegalStateException("Encryption error", ex);
        }
    }

    public String decrypt(String encoded) {
        if (encoded == null) return null;
        try {
            byte[] in = Base64.getDecoder().decode(encoded);
            ByteBuffer bb = ByteBuffer.wrap(in);
            byte[] iv = new byte[IV_BYTES];
            bb.get(iv);
            byte[] enc = new byte[bb.remaining()];
            bb.get(enc);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(enc), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Decryption error", ex);
        }
    }
}
