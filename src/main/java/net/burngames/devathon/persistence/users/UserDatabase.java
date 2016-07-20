package net.burngames.devathon.persistence.users;

import net.burngames.devathon.persistence.base.HikariDatabase;
import net.burngames.devathon.persistence.stmt.InsertCallableStatement;
import net.burngames.devathon.persistence.stmt.SelectCallableStatement;
import net.burngames.devathon.persistence.stmt.UpdateCallableStatement;
import net.burngames.devathon.routes.auth.AccountInfo;
import net.burngames.devathon.routes.auth.base.SimpleAccountInfo;

import java.sql.ResultSet;


public class UserDatabase extends HikariDatabase {

    private static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS users(" +
            "id INT(256) NOT NULL AUTO_INCREMENT, " +
            "username VARCHAR(256), " +
            "email VARCHAR(256), " +
            "PRIMARY KEY(id)," +
            "UNIQUE INDEX `id` (`id`)" +
            ")" +
            "ENGINE=InnoDB;";

    private static final String INSERT_USER = "INSERT INTO `users` VALUES (`username`, `email`) VALUES (?, ?)) ON DUPLICATE KEY UPDATE `email`=`email`;";

    private static final String SELECT_USER_BY_USERNAME = "SELECT `id`, `username`, `email` FROM `users` WHERE `username` = ?";

    public UserDatabase() {
        super(Runtime.getRuntime().availableProcessors(), "users", CREATE_USERS_TABLE);
    }

    public AccountInfo addUser(String username, String email) {
        try {
            ResultSet generated = new InsertCallableStatement(this, INSERT_USER, new Object[]{username, email}).call();
            if (generated.first()) {
                return new SimpleAccountInfo(generated.getInt("id"), username, email);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error adding user " + username + " to database, ", e);
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
        }

        return info;
    }

}
