package net.burngames.devathon.persistence.base;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import net.burngames.devathon.Website;
import net.burngames.devathon.persistence.Database;
import net.burngames.devathon.persistence.stmt.UpdateCallableStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Abstract implementation of {@link Database} that
 * uses a {@link HikariPool} to manage concurrent connection.
 *
 * @author Kenny
 */
public abstract class HikariDatabase implements Database {

    private final HikariDataSource pool;

    public HikariDatabase(int poolSize, String tableName, String... initQueries) {
        Properties pr = Website.getProperties();

        String url = pr.getProperty("database.jdbc.url"),
                user = pr.getProperty("database.user"),
                pass = pr.getProperty("database.pass");

        HikariConfig conf = new HikariConfig();

        conf.setJdbcUrl(url);
        conf.setUsername(user);
        conf.setPassword(pass);
        conf.setMaximumPoolSize(poolSize);

        pool = new HikariDataSource(conf);

        // test pool
        try (Connection c = getConnection()) {
            c.isValid(poolSize);

            // connection's valid, run the initial queries
            initialQueries(initQueries);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database " + url + ", " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error running init queries for database " + url + ", " + e.getMessage());
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    @Override
    public void closeConnections() {
        pool.close();
    }

    @Override
    public void initialQueries(String... queries) throws Exception {
        if (queries == null || queries.length == 0) {
            return;
        }

        ExceptionalConsumer<String> consumeQuery = str -> new UpdateCallableStatement(this, str, new Object[0]).call();

        Arrays.stream(queries).forEach(consumeQuery);
    }

    /**
     * Functional interface for avoiding checked exception in Consumer.
     * <p>
     * Lazy and unnecessary.
     *
     * @param <T> the type to pass to {@link ExceptionalConsumer#acceptExceptionally(Object)}
     * @author Kenny
     */
    @FunctionalInterface
    interface ExceptionalConsumer<T> extends Consumer<T> {
        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         */
        void acceptExceptionally(T t) throws Exception;

        @Override
        default void accept(T t) {
            try {
                acceptExceptionally(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
