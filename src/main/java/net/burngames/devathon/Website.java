package net.burngames.devathon;

import net.burngames.devathon.routes.AuthenticationRoute;
import net.burngames.devathon.routes.RegisterRoute;
import net.burngames.devathon.routes.RouteException;
import net.burngames.devathon.routes.RouteExceptionRoute;
import spark.Spark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractCollection;
import java.util.Properties;

/**
 * @author PaulBGD
 */
public class Website {

    private static Properties properties;

    public static void main(String[] strings) throws IOException {
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

        System.out.println("Starting in dev mode: " + isDevelopment());
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "1234"));
        int threads = Integer.parseInt(System.getenv().getOrDefault("THREADS", "6"));
        try {
            Spark.port(port);
            Spark.threadPool(threads);
            if (isDevelopment()) {
                Spark.staticFiles.externalLocation(new File("src/main/resources").getAbsolutePath());
            } else {
                Spark.staticFiles.location("/public");
                Spark.staticFiles.expireTime(60 * 60 * 24 * 7); // cache for a week
            }
            Spark.get("/register", new RegisterRoute());
            Spark.get("/authentication", new AuthenticationRoute());

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

    public static boolean isDevelopment() {
        return System.getenv().containsKey("DEV");
    }

}
