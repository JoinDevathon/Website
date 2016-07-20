package net.burngames.devathon.routes.auth;

import java.util.function.Consumer;


public interface UserRepository
{

    /**
     * Get the account info for the given username if loaded, or 
     * null otherwise. Intended to be used after a call to
     * {@link UserRepository#isUserLoaded(String)}.
     * 
     * @param username the user's username
     * @return the user's {@link AccountInfo}, or <code>null</code>
     * if the user is not loaded.
     */
    AccountInfo getUser(String username);
    
    /**
     * Returns true if the user's account info is cached in memory.
     * 
     * @param username the username of the user
     * @return <code>true</code> if the user's account info is loaded.
     * <code>false</code> otherwise.
     */
    boolean isUserLoaded(String username);
    
    /**
     * Attempts to load the user from the database if they're not
     * in memory, and then runs the supplied Consumer.
     * <p>
     * If the user is already loaded, runs the Consumer immediatedly.
     * 
     * @param username the user's username
     * @param onFinish the Consumer to run when finished 
     */
    void loadUserAndRun(String username, Consumer<AccountInfo> onFinish);
}
