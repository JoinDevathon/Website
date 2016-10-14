package net.burngames.devathon.persistence.users;

import net.burngames.devathon.persistence.base.HikariDatabase;
import net.burngames.devathon.persistence.stmt.CallableStatement;
import net.burngames.devathon.persistence.stmt.InsertCallableStatement;
import net.burngames.devathon.routes.auth.AccountInfo;
import net.burngames.devathon.routes.trophy.Trophy;

/**
 * @author PaulBGD
 */
public class TrophyDatabase extends HikariDatabase {

    private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS `devathon`";
    private static final String CREATE_TROPHY_TABLE = "CREATE TABLE IF NOT EXISTS `devathon`.`trophy`(" +
            "id INT(255) NOT NULL, " +
            "trophy VARCHAR(255), " +
            "name VARCHAR(255), " +
            "KEY(id)" +
            ")" +
            "ENGINE=InnoDB;";

    private static final String ADD_TROPHY = "INSERT INTO `devathon`.`trophy` (`id`,`trophy`,`name`) VALUES (?,?,?)";

    public TrophyDatabase() {
        super(2, CREATE_DATABASE, CREATE_TROPHY_TABLE);
    }

    public void addTrophy(AccountInfo info, Trophy trophy) {
        InsertCallableStatement statement = new InsertCallableStatement(this, ADD_TROPHY, new Object[]{info.getId(), trophy.getTrophy(), trophy.getName()});
        try {
            statement.call();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add trophy " + trophy.getTrophy() + " for user " + info.getId());
        } finally {
            statement.close();
        }
    }
}
