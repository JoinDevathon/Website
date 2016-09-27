package net.burngames.devathon.persistence;

import com.google.common.base.Joiner;
import net.burngames.devathon.Website;
import net.burngames.devathon.routes.RouteException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import spark.Request;
import spark.Response;

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

    public String init(Request request, Response response) throws GeneralSecurityException, RouteException {
        if (request.cookies().containsKey("devathon-session")) {
            return Website.getTokenGenerator().getToken(request.cookie("devathon-session"));
        }
        TokenGenerator.TokenAndClientToken tokens = Website.getTokenGenerator().generateTokens();
        response.cookie("devathon-session", tokens.clientToken);
        return tokens.token;
    }

    public SessionObject getSession(String token) {
        Jedis resource = this.pool.getResource();
        String result = resource.get(this.prefix + token);
        resource.close();
        if (result == null) {
            return null;
        }
        JSONObject object = new JSONObject(result);
        return new SessionObject(object.getJSONObject("data"), object.getLong("time") - System.currentTimeMillis());
    }

    public void setSession(String token, JSONObject object) {
        this.getLock(token); // get a lock so we don't override any previously set data
        SessionObject sessionObject = new SessionObject(object, System.currentTimeMillis() + this.ttl);

        Jedis resource = this.pool.getResource();
        resource.set(this.prefix + token, sessionObject.toJSON().toString(), "NX", "PX", this.ttl);
        resource.close();
        this.deleteLock(token);
    }

    public void deleteSession(String token) {
        this.getLock(token);

        Jedis resource = this.pool.getResource();
        resource.del(this.prefix + token);
        resource.close();

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
            Jedis resource = this.pool.getResource();
            String result = resource.set(lockKey, "L", "NX", "PX", this.lockTtl);
            resource.close();
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
