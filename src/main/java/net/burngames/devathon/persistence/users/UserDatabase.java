package net.burngames.devathon.persistence.users;

import com.google.common.collect.ImmutableMap;
import net.burngames.devathon.persistence.base.HikariDatabase;
import net.burngames.devathon.persistence.stmt.CallableStatement;
import net.burngames.devathon.persistence.stmt.InsertCallableStatement;
import net.burngames.devathon.persistence.stmt.SelectCallableStatement;
import net.burngames.devathon.persistence.stmt.UpdateCallableStatement;
import net.burngames.devathon.routes.auth.AccountInfo;
import net.burngames.devathon.routes.auth.base.SimpleAccountInfo;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


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

    private static final String SELECT_USER_BY_USERNAME = "SELECT `users`.*, `trophy`.* FROM `devathon`.`users` LEFT JOIN `devathon`.`trophy` ON `devathon`.`users`.`id` = `devathon`.`trophy`.`id` WHERE `users`.`username` = ?";
    private static final String SELECT_USER_BY_ID = "SELECT `users`.*, `trophy`.* FROM `devathon`.`users` LEFT JOIN `devathon`.`trophy` ON `devathon`.`users`.`id` = `devathon`.`trophy`.`id` WHERE `users`.`id` = ?";
    private static final String SET_EMAIL = "UPDATE `devathon`.`users` SET `email` = ? WHERE `id` = ?";
    private static final String SET_BEAM = "UPDATE `devathon`.`users` SET `beam` = ? WHERE `id` = ?";
    private static final String SET_TWITCH = "UPDATE `devathon`.`users` SET `twitch` = ? WHERE `id` = ?";
    private static final String SET_TWITTER = "UPDATE `devathon`.`users` SET `twitter` = ? WHERE `id` = ?";

    public UserDatabase() {
        super(Math.min(Runtime.getRuntime().availableProcessors(), 2), CREATE_DATABASE, CREATE_USERS_TABLE);
    }

    public AccountInfo addUser(String username, String email) {
        if (email == null) {
            email = "";
        }
        CallableStatement<ResultSet> statement = new InsertCallableStatement(this, INSERT_USER, new Object[]{username, email});
        try (ResultSet generated = statement.call()) {
            if (generated.first()) {
                return new SimpleAccountInfo(generated.getInt(1), username, email, "", "", "", ImmutableMap.of());
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

    public void setEmail(int id, String email) {
        UpdateCallableStatement statement = new UpdateCallableStatement(this, SET_EMAIL, new Object[]{email, id});
        try {
            statement.call();
        } catch (Exception e) {
            throw new RuntimeException("Error setting id " + id + "'s email to " + email);
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
                        boolean assigned = false;
                        int id = -1;
                        String email = null;
                        String beam = null;
                        String twitch = null;
                        String twitter = null;
                        Map<String, String> trophies = new HashMap<>();

                        while (results.next()) {
                            if (!assigned) {
                                assigned = true;
                                id = results.getInt("id");
                                email = results.getString("email");
                                beam = results.getString("beam");
                                twitch = results.getString("twitch");
                                twitter = results.getString("twitter");
                            }
                            String trophy = results.getString("trophy");
                            if (trophy != null) {
                                trophies.put(results.getString("trophy"), results.getString("name"));
                            }
                        }

                        return new SimpleAccountInfo(id, username, email, beam, twitch, twitter, trophies);
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
                        boolean assigned = false;
                        String username = null;
                        String email = null;
                        String beam = null;
                        String twitch = null;
                        String twitter = null;
                        Map<String, String> trophies = new HashMap<>();

                        while (results.next()) {
                            if (!assigned) {
                                assigned = true;
                                username = results.getString("username");
                                email = results.getString("email");
                                beam = results.getString("beam");
                                twitch = results.getString("twitch");
                                twitter = results.getString("twitter");
                            }
                            String trophy = results.getString("trophy");
                            if (trophy != null) {
                                trophies.put(results.getString("trophy"), results.getString("name"));
                            }
                        }

                        return new SimpleAccountInfo(id, username, email, beam, twitch, twitter, trophies);
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
