package net.burngames.devathon.routes.auth.base;

import net.burngames.devathon.routes.auth.AccountInfo;

import java.util.UUID;

/**
 * Basic implementation of {@link AccountInfo}.
 *
 * @author Kenny
 */
public class SimpleAccountInfo implements AccountInfo {

    private final UUID uuid;

    private final String username;

    private final String email;

    public SimpleAccountInfo(UUID uuid, String username, String email) {
        this.uuid = uuid;
        this.username = username;
        this.email = email;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String toString() {
        return username + "( " + uuid + ", " + ", " + email + " )";
    }

}
