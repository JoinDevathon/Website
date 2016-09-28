package net.burngames.devathon.routes;

import net.burngames.devathon.Website;
import spark.Request;
import spark.Response;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @author PaulBGD
 */
public class RegisterRoute<Void> implements TypedRoute {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String BASE_URL = "https://github.com/login/oauth/authorize";
    private static final String CLIENT_ID = Website.getProperties().getProperty("github_client_id");
    private static final String SCOPE = "user:email";

    private final byte[] encryptionKey;
    private final String statePrefix;

    public RegisterRoute() {
        this.encryptionKey = Website.getProperties().getProperty("state_encryption_key").getBytes();
        this.statePrefix = Website.getProperties().getProperty("state_prefix");
    }

    @Override
    public Void handleTyped(Request request, Response response) throws Exception {
        String state = this.statePrefix; // our prefix, which we check for later
        // to disable fixed length attacks, append a random amount of bytes as a suffix
        int extra = RANDOM.nextInt(15) + 12;
        byte[] stateBytes = state.getBytes();
        byte[] input = new byte[stateBytes.length + extra];
        System.arraycopy(stateBytes, 0, input, 0, stateBytes.length);
        for (int i = 0; i < extra; i++) {
            input[stateBytes.length + i] = (byte) RANDOM.nextInt(255);
        }

        final byte[] ivBytes = new byte[16];
        RANDOM.nextBytes(ivBytes);

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        final SecretKeySpec key = new SecretKeySpec(this.encryptionKey, "AES");
        final IvParameterSpec iv = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        final byte[] encrypted = new byte[cipher.getOutputSize(input.length)];
        final int length = cipher.update(input, 0, input.length, encrypted, 0);
        cipher.doFinal(encrypted, length);

        byte[] finalState = new byte[ivBytes.length + encrypted.length];
        System.arraycopy(ivBytes, 0, finalState, 0, ivBytes.length);
        System.arraycopy(encrypted, 0, finalState, ivBytes.length, encrypted.length);

        final String secureState = Base64.getEncoder().encodeToString(finalState);

        response.redirect(BASE_URL + "?client_id=" + CLIENT_ID + "&scope=" + SCOPE + "&state=" + secureState);
        return null;
    }
}
