package net.burngames.devathon.routes.auth.base;

import net.burngames.devathon.routes.auth.AccountInfo;

import java.util.UUID;

/**
 * Basic implementation of {@link AccountInfo}.
 *
 * @author Kenny
 */
public class SimpleAccountInfo implements AccountInfo {

    private final int id;

    private final String username;

    private final String email;

    public SimpleAccountInfo(int id, String username, String email) {
        this.id = id;
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
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return username + "( " + id + ", " + ", " + email + " )";
    }

}
