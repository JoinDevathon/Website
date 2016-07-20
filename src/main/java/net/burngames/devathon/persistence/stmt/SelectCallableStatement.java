package net.burngames.devathon.persistence.stmt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import net.burngames.devathon.persistence.Database;

public class SelectCallableStatement<T> extends CallableStatement<T>
{        
    private final Function<ResultSet, T> function;
    
    public SelectCallableStatement(String sql, Object[] args, Database db, Function<ResultSet, T> function)
    {
        super(db, sql, args);

        this.function = function;
    }

    @Override
    protected T work() throws SQLException
    {
        prepareStatement(query);
        
        for (int i = 0; i < args.length; i++)
        {
            stmt.setObject(i + 1, args[i]);
        }
        
        return function.apply(stmt.executeQuery());
    }
    
}
