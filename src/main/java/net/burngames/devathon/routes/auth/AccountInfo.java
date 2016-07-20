package net.burngames.devathon.routes.auth;

import java.util.UUID;

/**
 * Represents an authenticated user.
 * 
 * @author Kenny
 */
public interface AccountInfo
{

    /**
     * Get the unique integer ID assigned to this user.
     * @return the unique ID assigned to this user.
     */
    UUID getUniqueId();
    
    /**
     * @return the user's Github username.
     */
    String getUsername();
    
    /**
     * @return the user's Github email.
     */
    String getEmail();
    
}
