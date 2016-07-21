package net.burngames.devathon.routes.auth.base;

import com.google.common.collect.ImmutableMap;
import net.burngames.devathon.routes.auth.AccountInfo;

import java.util.Map;
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
    public Map<String, Object> toMap() {
        return ImmutableMap.of(
                "id", this.id,
                "username", this.username,
                "email", this.email
        );
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
