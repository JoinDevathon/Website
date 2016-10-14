package net.burngames.devathon.persistence;

import net.burngames.devathon.routes.RouteException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import redis.clients.jedis.JedisPool;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;

/**
 * @author PaulBGD
 */
public class TokenGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final long ttl = 24 * 60 * 60 * 1000; // our sessions expire every day

    private final JedisPool pool;
    private final String key;

    private final String prefix = "devathon:";

    public TokenGenerator(JedisPool pool, String key) {
        this.pool = pool;
        this.key = key;
    }

    public TokenAndClientToken generateTokens() throws GeneralSecurityException {
        byte[] token;
        String tokenString;
        // check to make sure our token is random
        do {
            token = this.generateToken();
            tokenString = Hex.encodeHexString(token);
        } while (this.pool.getResource().exists(this.prefix + tokenString));

        String clientToken = this.generateClientToken(token);
        return new TokenAndClientToken(tokenString, clientToken);
    }

    private byte[] generateToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private String generateClientToken(byte[] token) throws GeneralSecurityException {
        SecretKeySpec keySpec = new SecretKeySpec(this.key.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        byte[] encryptionKey = mac.doFinal("devathon-enc".getBytes());

        mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        byte[] signatureKey = mac.doFinal("devathon-mac".getBytes());

        byte[] iv = this.generateToken(); // technically a token and iv are two different things, but in our case they're both 16 random bytes
        ByteBuffer value = ByteBuffer.allocate(token.length + 32); // 32 is how many bytes a long takes up
        value.put(token);
        value.putLong(System.currentTimeMillis() + this.ttl);
        byte[] fullToken = value.array();

        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(fullToken); // okay, now our token + time is encrypted in AES using our encryption key

        mac = Mac.getInstance("HmacSHA256");
        keySpec = new SecretKeySpec(signatureKey, "HmacSHA256");
        mac.init(keySpec);
        mac.update(iv);
        mac.update(encrypted);
        byte[] hmac = mac.doFinal();

        ByteBuffer buffer = ByteBuffer.allocate(iv.length + 2 + hmac.length + encrypted.length);
        buffer.put(iv);
        buffer.putShort((short) hmac.length); // store length so that we can check it later
        buffer.put(hmac);
        buffer.put(encrypted);

        return Base64.getEncoder().withoutPadding().encodeToString(buffer.array());
    }

    public String getToken(String clientToken) throws RouteException, GeneralSecurityException {
        byte[] encrypted = Base64.getDecoder().decode(clientToken.getBytes());
        if (encrypted.length < 16 + 2 + 32 || encrypted.length > 128) {
            throw new RouteException("Invalid token.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(encrypted);
        byte[] iv = new byte[16];
        buffer.get(iv);
        short hmacLength = buffer.getShort();
        if (hmacLength < 16 || hmacLength > 64) {
            throw new RouteException("Invalid token.");
        }
        byte[] hmac = new byte[hmacLength];
        buffer.get(hmac);
        byte[] encryptedData = new byte[encrypted.length - iv.length - 2 - hmac.length];
        buffer.get(encryptedData);

        SecretKeySpec keySpec = new SecretKeySpec(this.key.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        byte[] encryptionKey = mac.doFinal("devathon-enc".getBytes());

        mac = Mac.getInstance("HmacSHA256");
        keySpec = new SecretKeySpec(this.key.getBytes(), "HmacSHA256");
        mac.init(keySpec);
        byte[] signatureKey = mac.doFinal("devathon-mac".getBytes());

        mac = Mac.getInstance("HmacSHA256");
        keySpec = new SecretKeySpec(signatureKey, "HmacSHA256");
        mac.init(keySpec);
        mac.update(iv);
        mac.update(encryptedData);
        byte[] expectedHmac = mac.doFinal();

        if (expectedHmac.length != hmac.length) {
            throw new RouteException("Invalid token.");
        }
        for (int i = 0; i < hmacLength; i++) {
            if (expectedHmac[i] != hmac[i]) {
                throw new RouteException("Invalid token.");
            }
        }
        // okay, they didn't modify the hmac. let's decrypt the data now
        SecretKeySpec key = new SecretKeySpec(encryptionKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(encryptedData);
        byte[] tokenBytes = new byte[decrypted.length - 32];
        ByteBuffer decryptedBuffer = ByteBuffer.wrap(decrypted);
        decryptedBuffer.get(tokenBytes);
        long time = decryptedBuffer.getLong();
        if (System.currentTimeMillis() - time > 0) {
            throw new RouteException("Your token has expired, please log in again.");
        }
        return Hex.encodeHexString(tokenBytes);
    }

    public static class TokenAndClientToken {
        public final String token;
        public final String clientToken;

        public TokenAndClientToken(String token, String clientToken) {
            this.token = token;
            this.clientToken = clientToken;
        }

        @Override
        public String toString() {
            return "TokenAndClientToken{" +
                    "token='" + token + '\'' +
                    ", clientToken='" + clientToken + '\'' +
                    '}';
        }
    }
}
