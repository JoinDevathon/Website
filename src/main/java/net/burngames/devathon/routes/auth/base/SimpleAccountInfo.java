package net.burngames.devathon.routes.auth.base;

import net.burngames.devathon.routes.auth.UserInfo;

/**
 * Basic implementation of {@link UserInfo}.
 * 
 * @author Kenny
 */
public class SimpleAccountInfo implements UserInfo
{

    private final String username;
    
    private final String email;
    
    public SimpleAccountInfo(String username, String email)
    {
        this.username = username;
        this.email = email;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public String getEmail()
    {
        return email;
    }
    
}
