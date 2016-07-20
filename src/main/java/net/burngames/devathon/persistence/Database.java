package net.burngames.devathon.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base database API
 * 
 * @author Kenny
 */
public interface Database
{

    /**
     * Attemps to get a connection to the database
     * 
     * @return the connection
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;
    
    /**
     * Attempts to close connections to the database
     */
    void closeConnections();
    
    /**
     * Create initial required tables if they do not exist.
     * 
     * @param queries the queries to run. Will be ignored
     * if null or empty
     */
    void initialQueries(String... queries) throws Exception;
    
}
