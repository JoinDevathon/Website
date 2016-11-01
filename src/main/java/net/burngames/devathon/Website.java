package net.burngames.devathon;

import net.burngames.devathon.persistence.Sessions;
import net.burngames.devathon.persistence.TokenGenerator;
import net.burngames.devathon.persistence.users.TrophyDatabase;
import net.burngames.devathon.persistence.users.UserDatabase;
import net.burngames.devathon.routes.*;
import net.burngames.devathon.routes.auth.AuthenticationRoute;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * @author PaulBGD
 */
public class Website {

    private static Properties properties;
    private static UserDatabase userDatabase;
    private static TrophyDatabase trophyDatabase;
    private static JedisPool pool;
    private static Sessions sessions;
    private static TokenGenerator tokenGenerator;

    public static void main(String[] strings) throws IOException, GeneralSecurityException {
        if (isDevelopment()) {
            // unless you're ready to die, don't enable this
            //System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        }

        properties = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            properties.load(input);
        } finally {
            input.close();
        }
        System.out.println("Loading database..");
        Website.userDatabase = new UserDatabase();
        Website.trophyDatabase = new TrophyDatabase();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPoolConfig.setMaxTotal(32);
        Website.pool = new JedisPool(jedisPoolConfig, properties.getProperty("jedis.host"));
        Website.sessions = new Sessions(Website.pool);
        Website.tokenGenerator = new TokenGenerator(Website.pool, properties.getProperty("token_key"));

        System.out.println("Starting in dev mode: " + isDevelopment());
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "1234"));
        int threads = Integer.parseInt(System.getenv().getOrDefault("THREADS", "6"));
        try {
            Spark.ipAddress("10.0.0.23");
            Spark.port(port);
            Spark.threadPool(threads);

            if (isDevelopment()) {
                Spark.staticFiles.externalLocation(new File(System.getenv().getOrDefault("PUBLIC", "src/main/resources/public")).getAbsolutePath());
            } else {
                Spark.staticFiles.location("/public");
                Spark.staticFiles.expireTime(60 * 60 * 24 * 7); // cache for a week
            }
            Spark.get("/", new HomeRoute(), new MustacheTemplateEngine());
            Spark.get("/register", new RegisterRoute());
            Spark.get("/authentication", new AuthenticationRoute());
            Spark.get("/account", new AccountRoute(), new MustacheTemplateEngine());
            Spark.get("/user/:user", new OtherAccountRoute(), new MustacheTemplateEngine());
            Spark.get("/account/update", new AccountUpdateRoute());
            Spark.post("/account/details", new AccountDetailsRoute());
            Spark.get("/error", new ErrorRoute(), new MustacheTemplateEngine());

            Spark.after((request, response) -> {
                response.header("Content-Encoding", "gzip"); // do gzip
            });

            Spark.exception(RouteException.class, new RouteExceptionRoute());

            Spark.exception(Exception.class, (e, request, response) -> {
                e.printStackTrace();
                System.exit(1);
            });
            Spark.awaitInitialization();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ended with stacktrace.");
            Spark.stop();
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static UserDatabase getUserDatabase() {
        return userDatabase;
    }

    public static TrophyDatabase getTrophyDatabase() {
        return trophyDatabase;
    }

    public static JedisPool getPool() {
        return pool;
    }

    public static Sessions getSessions() {
        return sessions;
    }

    public static TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    public static boolean isDevelopment() {
        return System.getenv().containsKey("DEV");
    }

}
