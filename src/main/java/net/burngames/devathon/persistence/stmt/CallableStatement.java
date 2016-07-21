package net.burngames.devathon.persistence.stmt;

import net.burngames.devathon.persistence.Database;

import java.sql.*;
import java.util.concurrent.Callable;

public abstract class CallableStatement<T> implements Callable<T> {

    protected final String query;

    protected final Object[] args;

    protected final Database db;

    protected Connection connection;

    protected PreparedStatement stmt;

    protected ResultSet result;

    public CallableStatement(Database db, String query, Object[] args) {
        this.db = db;
        this.query = query;
        this.args = args;
    }

    @Override
    public T call() throws Exception {
        connection = db.getConnection();
        if (connection != null) {
            return work();
        } else {
            throw new SQLException("A connection to the database couldn't be established");
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
        }
        if (result != null) {
            try {
                result.close();
            } catch (SQLException ex) {
            }
        }
    }

    protected abstract T work() throws SQLException;

    protected void prepareStatement(String str) throws SQLException {
        this.prepareStatement(str, false);
    }

    protected void prepareStatement(String str, boolean generateKeys) throws SQLException {
        if (stmt != null) {
            stmt.close();
        }

        if (generateKeys) {
            stmt = connection.prepareStatement(str, Statement.RETURN_GENERATED_KEYS);
        } else {
            stmt = connection.prepareStatement(str);
        }
    }

    protected void executeStatement() throws SQLException {
        if (result != null) {
            result.close();
        }

        result = stmt.executeQuery();
    }

}
