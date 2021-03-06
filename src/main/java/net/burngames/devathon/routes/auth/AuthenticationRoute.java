package net.burngames.devathon.routes.auth;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import net.burngames.devathon.Website;
import net.burngames.devathon.persistence.Sessions;
import net.burngames.devathon.routes.RouteException;
import net.burngames.devathon.routes.TypedRoute;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @author PaulBGD
 */
public class AuthenticationRoute implements TypedRoute<Void> {

    private final byte[] encryptionKey;
    private final byte[] statePrefix;

    private final String clientId;
    private final String clientSecret;

    public AuthenticationRoute() {
        this.encryptionKey = Website.getProperties().getProperty("state_encryption_key").getBytes();
        this.statePrefix = Website.getProperties().getProperty("state_prefix").getBytes();

        this.clientId = Website.getProperties().getProperty("github_client_id");
        this.clientSecret = Website.getProperties().getProperty("github_client_secret");
    }

    @Override
    public Void handleTyped(Request request, Response response) throws Exception {
        if (request.queryParams("code") == null || request.queryParams("state") == null) {
            throw new RouteException("Invalid authentication request.");
        }
        String state = request.queryParams("state").replace(' ', '+'); // stupid spark removes these.. i guess we should've escaped them
        byte[] encrypted = Base64.getDecoder().decode(state);
        if (encrypted.length <= 16) {
            throw new RouteException("Invalid state.");
        }
        byte[] iv = new byte[16];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);
        byte[] encryptedState = new byte[encrypted.length - 16]; // everything but the iv
        System.arraycopy(encrypted, 16, encryptedState, 0, encryptedState.length);

        SecretKeySpec key = new SecretKeySpec(this.encryptionKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        byte[] decrypted = new byte[cipher.getOutputSize(encryptedState.length)];
        int length = cipher.update(encryptedState, 0, encryptedState.length, decrypted, 0);
        cipher.doFinal(decrypted, length);

        // compare the bytes for our prefix
        for (int i = 0; i < this.statePrefix.length; i++) {
            if (decrypted[i] != this.statePrefix[i]) {
                throw new RouteException("Invalid state.");
            }
        }

        // now we go check github
        HttpResponse<JsonNode> jsonResponse = Unirest.post("https://github.com/login/oauth/access_token")
                .header("accept", "application/json")
                .queryString("client_id", this.clientId)
                .queryString("client_secret", this.clientSecret)
                .queryString("code", request.queryParams("code"))
                .queryString("state", state)
                .asJson();
        JSONObject json = jsonResponse.getBody().getObject();
        if (!json.has("access_token")) {
            throw new RouteException("Failed to authenticate.");
        }
        String accessToken = json.getString("access_token");

        // now lets retrieve our user info
        HttpResponse<JsonNode> userResponse = Unirest.get("https://api.github.com/user")
                .header("accept", "application/json")
                .queryString("access_token", accessToken)
                .asJson();
        JSONObject userJson = userResponse.getBody().getObject();
        if (!userJson.has("login")) {
            System.out.println("Got " + userResponse.getBody().toString() + " which is missing login/email");
            throw new RouteException("Failed to authenticate."); // something happened and our id that should be here is missing
        }

        try {

            String username = userJson.getString("login");
            String email = userJson.isNull("email") ? null : userJson.getString("email");

            final AccountInfo account = Website.getUserDatabase().addUser(username, email);
            Sessions sessions = Website.getSessions();
            String token = sessions.init(request, response);
            JSONObject object = new JSONObject();
            object.put("id", account.getId());
            sessions.setSession(token, object);
        } catch(RouteException e) {
            // bad token, have them reset
            response.removeCookie("devathon-session");
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("JSON: " + userJson.toString());
            throw new RouteException("Failed to authenticate.");
        }

        response.redirect("/account");
        return null;
    }
}
