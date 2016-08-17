package net.burngames.devathon.persistence;

import net.burngames.devathon.Website;
import net.burngames.devathon.routes.RouteException;
import org.json.JSONObject;
import redis.clients.jedis.JedisPool;
import spark.Request;

import java.security.GeneralSecurityException;

/**
 * @author PaulBGD
 */
public class Sessions {

    private final JedisPool pool;

    private final String prefix = "devathon:";
    private final String lockSuffix = ":l";
    private final long lockTtl = 80 * 1000; // locks time out in 8s
    private final long lockRetry = 5; // retry every 5ms
    private final long ttl = 24 * 60 * 60 * 1000; // our sessions expire every day

    public Sessions(JedisPool pool) {
        this.pool = pool;
    }

    public String init(Request request) throws GeneralSecurityException, RouteException {
        if (request.cookies().containsKey("devathon-session")) {
            return Website.getTokenGenerator().getToken(request.cookie("devathon-session"));
        }
        TokenGenerator.TokenAndClientToken tokens = Website.getTokenGenerator().generateTokens();
        request.cookies().put("devathon-session", tokens.clientToken);
        return tokens.token;
    }

    public SessionObject getSession(String token) {
        String result = this.pool.getResource().get(this.prefix + token);
        JSONObject object = new JSONObject(result);
        return new SessionObject(object.getJSONObject("data"), object.getLong("time") - System.currentTimeMillis());
    }

    public void setSession(String token, JSONObject object) {
        this.getLock(token); // get a lock so we don't override any previously set data
        SessionObject sessionObject = new SessionObject(object, System.currentTimeMillis() + this.ttl);

        this.pool.getResource().set(this.prefix + token, sessionObject.toJSON().toString(), "NX", "PX", this.ttl);
        this.deleteLock(token);
    }

    public void deleteSession(String token) {
        this.getLock(token);

        this.pool.getResource().del(this.prefix + token);

        this.deleteLock(token);
    }

    private void deleteLock(String token) {
        this.pool.getResource().del(this.prefix + token + this.lockSuffix);
    }

    /**
     * This is blocking, watch out
     */
    private void getLock(String token) {
        String lockKey = this.prefix + token + this.lockSuffix;
        int i = 0;
        while (true) {
            if (++i % 100 == 0) {
                System.out.println("Been locked out from " + lockKey + " " + i + " times!");
            }
            String result = this.pool.getResource().set(lockKey, "L", "NX", "PX", this.lockTtl);
            if (result == null) {
                try {
                    Thread.sleep(this.lockRetry);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break; // we've finally got a lock
            }
        }
    }

    public static class SessionObject {
        public final JSONObject json;
        public final long timeLeft;

        public SessionObject(JSONObject json, long timeLeft) {
            this.json = json;
            this.timeLeft = timeLeft;
        }

        public JSONObject toJSON() {
            JSONObject object = new JSONObject();
            object.put("data", this.json);
            object.put("time", this.timeLeft);
            return object;
        }
    }

}