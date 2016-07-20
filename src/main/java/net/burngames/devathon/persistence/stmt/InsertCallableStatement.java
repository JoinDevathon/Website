package net.burngames.devathon.persistence.stmt;

import net.burngames.devathon.persistence.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author PaulBGD
 */
public class InsertCallableStatement extends CallableStatement<ResultSet> {
    public InsertCallableStatement(Database db, String query, Object[] args) {
        super(db, query, args);
    }

    @Override
    protected ResultSet work() throws SQLException {
        prepareStatement(query);

        for (int i = 0; i < args.length; i++) {
            stmt.setObject(i + 1, args[i]);
        }

        stmt.executeUpdate();

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            return generatedKeys;
        }
    }
}
