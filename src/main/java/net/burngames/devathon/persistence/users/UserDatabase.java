package net.burngames.devathon.persistence.users;

import net.burngames.devathon.persistence.base.HikariDatabase;
import net.burngames.devathon.persistence.stmt.SelectCallableStatement;
import net.burngames.devathon.persistence.stmt.UpdateCallableStatement;
import net.burngames.devathon.routes.auth.AccountInfo;
import net.burngames.devathon.routes.auth.base.SimpleAccountInfo;

import java.nio.ByteBuffer;
import java.util.UUID;


public class UserDatabase extends HikariDatabase {

    private static final String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS users(uniqueId BINARY(16) NOT NULL, "
            + " username VARCHAR(256), email VARCHAR(256), PRIMARY KEY(uniqueId));";

    private static final String INSERT_USER = "INSERT INTO users VALUES (uniqueId, username, email) VALUES (?, ?, ?));";

    private static final String SELECT_USER_BY_USERNAME = "SELECT uniqueId, username, email FROM users WHERE username = ?";

    public UserDatabase() {
        super(Runtime.getRuntime().availableProcessors(), "users", CREATE_USERS_TABLE);
    }

    public void addUser(AccountInfo account) {
        try {
            new UpdateCallableStatement(this, INSERT_USER, new Object[]{account.getUniqueId(), account.getUsername(), account.getEmail()}).call();
        } catch (Exception e) {
            throw new RuntimeException("Error adding user " + account + " to database, " + e);
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

                        UUID uniqueId = convert(results.getBytes("uniqueId"));
                        String email = results.getString("email");

                        return new SimpleAccountInfo(uniqueId, username, email);
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

    private UUID convert(byte[] ba) {
        ByteBuffer bb = ByteBuffer.wrap(ba);

        return new UUID(bb.getLong(), bb.getLong());
    }

    private byte[] convert(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);

        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }
}
