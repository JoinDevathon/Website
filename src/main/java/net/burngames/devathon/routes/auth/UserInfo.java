package net.burngames.devathon.routes.auth;

/**
 * Represents an authenticated user.
 * 
 * @author Kenny
 */
public interface UserInfo
{

    /**
     * @return the user's Github username.
     */
    String getUsername();
    
    /**
     * @return the user's Github email.
     */
    String getEmail();
    
}
