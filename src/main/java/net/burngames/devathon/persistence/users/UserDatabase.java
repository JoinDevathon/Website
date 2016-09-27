package net.burngames.devathon.persistence.users;

import net.burngames.devathon.persistence.base.HikariDatabase;
import net.burngames.devathon.persistence.stmt.CallableStatement;
import net.burngames.devathon.persistence.stmt.InsertCallableStatement;
import net.burngames.devathon.persistence.stmt.SelectCallableStatement;
import net.burngames.devathon.persistence.stmt.UpdateCallableStatement;
import net.burngames.devathon.routes.auth.AccountInfo;
import net.burngames.devathon.routes.auth.base.SimpleAccountInfo;

import java.sql.ResultSet;


public class UserDatabase extends HikariDatabase {

    private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS `devathon`";
    private static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS `devathon`.`users`(" +
            "id INT(255) NOT NULL AUTO_INCREMENT, " +
            "username VARCHAR(255), " +
            "email VARCHAR(255), " +
            "beam VARCHAR(255) DEFAULT '', " +
            "twitch VARCHAR(255) DEFAULT '', " +
            "twitter VARCHAR(255) DEFAULT '', " +
            "PRIMARY KEY(id)," +
            "UNIQUE INDEX `username` (`username`)" +
            ")" +
            "ENGINE=InnoDB;";

    private static final String INSERT_USER = "INSERT INTO `devathon`.`users` (`username`, `email`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `email`=`email`";

    private static final String SELECT_USER_BY_USERNAME = "SELECT `id`, `email`, `beam`, `twitch`, `twitter` FROM `devathon`.`users` WHERE `username` = ?";
    private static final String SELECT_USER_BY_ID = "SELECT `username`, `email`, `beam`, `twitch`, `twitter` FROM `devathon`.`users` WHERE `id` = ?";
    private static final String SET_BEAM = "UPDATE `devathon`.`users` SET `beam` = ? WHERE `id` = ?";
    private static final String SET_TWITCH = "UPDATE `devathon`.`users` SET `twitch` = ? WHERE `id` = ?";
    private static final String SET_TWITTER = "UPDATE `devathon`.`users` SET `twitter` = ? WHERE `id` = ?";

    public UserDatabase() {
        super(Math.min(Runtime.getRuntime().availableProcessors(), 2), CREATE_DATABASE, CREATE_USERS_TABLE);
    }

    public AccountInfo addUser(String username, String email) {
        CallableStatement<ResultSet> statement = new InsertCallableStatement(this, INSERT_USER, new Object[]{username, email});
        try (ResultSet generated = statement.call()) {
            if (generated.first()) {
                return new SimpleAccountInfo(generated.getInt(1), username, email, "", "", "");
            } else {
                // user already exists, grab info
                return this.getUserByUsername(username);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error adding user " + username + " to database, ", e);
        } finally {
            statement.close();
        }
    }

    public void setBeam(int id, String beam) {
        UpdateCallableStatement statement = new UpdateCallableStatement(this, SET_BEAM, new Object[]{beam, id});
        try {
            statement.call();
        } catch (Exception e) {
            throw new RuntimeException("Error setting id " + id + "'s beam to " + beam);
        } finally {
            statement.close();
        }
    }

    public void setTwitch(int id, String twitch) {
        UpdateCallableStatement statement = new UpdateCallableStatement(this, SET_TWITCH, new Object[]{twitch, id});
        try {
            statement.call();
        } catch (Exception e) {
            throw new RuntimeException("Error setting id " + id + "'s twitch to " + twitch);
        } finally {
            statement.close();
        }
    }

    public void setTwitter(int id, String twitter) {
        UpdateCallableStatement statement = new UpdateCallableStatement(this, SET_TWITTER, new Object[]{twitter, id});
        try {
            statement.call();
        } catch (Exception e) {
            throw new RuntimeException("Error setting id " + id + "'s twitter to " + twitter);
        } finally {
            statement.close();
        }
    }

    public AccountInfo getUserByUsername(String username) {
        SelectCallableStatement<AccountInfo> stmt = new SelectCallableStatement<>(
                SELECT_USER_BY_USERNAME,
                new Object[]{username},
                this,
                results ->
                {
                    try {
                        if (!results.next()) {
                            return null;
                        }

                        int id = results.getInt("id");
                        String email = results.getString("email");
                        String beam = results.getString("beam");
                        String twitch = results.getString("twitch");
                        String twitter = results.getString("twitter");

                        return new SimpleAccountInfo(id, username, email, beam, twitch, twitter);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        AccountInfo info;

        try {
            info = stmt.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stmt.close();
        }

        return info;
    }

    public AccountInfo getUserById(int id) {
        SelectCallableStatement<AccountInfo> stmt = new SelectCallableStatement<>(
                SELECT_USER_BY_ID,
                new Object[]{id},
                this,
                results ->
                {
                    try {
                        if (!results.next()) {
                            return null;
                        }

                        String username = results.getString("username");
                        String email = results.getString("email");
                        String beam = results.getString("beam");
                        String twitch = results.getString("twitch");
                        String twitter = results.getString("twitter");

                        return new SimpleAccountInfo(id, username, email, beam, twitch, twitter);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        AccountInfo info;

        try {
            info = stmt.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stmt.close();
        }

        return info;
    }
}
