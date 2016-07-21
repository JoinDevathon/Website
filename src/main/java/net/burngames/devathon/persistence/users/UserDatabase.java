package net.burngames.devathon.persistence.users;

import net.burngames.devathon.persistence.base.HikariDatabase;
import net.burngames.devathon.persistence.stmt.CallableStatement;
import net.burngames.devathon.persistence.stmt.InsertCallableStatement;
import net.burngames.devathon.persistence.stmt.SelectCallableStatement;
import net.burngames.devathon.routes.auth.AccountInfo;
import net.burngames.devathon.routes.auth.base.SimpleAccountInfo;

import java.sql.ResultSet;


public class UserDatabase extends HikariDatabase {

    private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS `devathon`";
    private static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS `devathon`.`users`(" +
            "id INT(255) NOT NULL AUTO_INCREMENT, " +
            "username VARCHAR(255), " +
            "email VARCHAR(255), " +
            "PRIMARY KEY(id)," +
            "UNIQUE INDEX `username` (`username`)" +
            ")" +
            "ENGINE=InnoDB;";

    private static final String INSERT_USER = "INSERT INTO `devathon`.`users` (`username`, `email`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `email`=`email`";

    private static final String SELECT_USER_BY_USERNAME = "SELECT `id`, `username`, `email` FROM `devathon`.`users` WHERE `username` = ?";

    public UserDatabase() {
        super(Runtime.getRuntime().availableProcessors(), CREATE_DATABASE, CREATE_USERS_TABLE);
    }

    public AccountInfo addUser(String username, String email) {
        CallableStatement<ResultSet> statement = new InsertCallableStatement(this, INSERT_USER, new Object[]{username, email});
        try (ResultSet generated = statement.call()) {
            if (generated.first()) {
                return new SimpleAccountInfo(generated.getInt(1), username, email);
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

                        return new SimpleAccountInfo(id, username, email);
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
