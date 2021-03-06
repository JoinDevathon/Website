package net.burngames.devathon.routes.auth;

import java.util.Map;
import java.util.UUID;

/**
 * Represents an authenticated user.
 *
 * @author Kenny
 */
public interface AccountInfo {

    /**
     * Get the unique integer ID assigned to this user.
     *
     * @return the unique ID assigned to this user.
     */
    int getId();

    /**
     * @return the user's Github username.
     */
    String getUsername();

    /**
     * @return the user's Github email.
     */
    String getEmail();

    Map<String, String> getTrophies();

    /**
     * Returns a map containing `id`, `username`, `email`, and `trophies`
     * @return a map
     */
    Map<String, Object> toMap();

}
