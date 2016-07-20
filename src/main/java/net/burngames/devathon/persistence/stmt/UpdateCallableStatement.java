package net.burngames.devathon.persistence.stmt;

import net.burngames.devathon.persistence.Database;

import java.sql.SQLException;

public class UpdateCallableStatement extends CallableStatement<Integer> {

    public UpdateCallableStatement(Database db, String query, Object[] args) {
        super(db, query, args);
    }

    @Override
    protected Integer work() throws SQLException {
        prepareStatement(query);

        for (int i = 0; i < args.length; i++) {
            stmt.setObject(i + 1, args[i]);
        }

        return stmt.executeUpdate();
    }

}
