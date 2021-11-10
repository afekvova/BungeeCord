package ru.afek.auth.utils;

import ru.afek.bungeecord.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IpListCheck {

    private SQLConnection sqlConnection;

    public IpListCheck(SQLConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    public int getCountUser(String ip) {
        try (PreparedStatement statament = this.sqlConnection.getConnection().prepareStatement("select count(Name) from Auth where `Ip` = '" + ip + "';");
             ResultSet set = statament.executeQuery()) {
            while (set.next()) {
                int count = set.getInt(1);
                set.close();
                return count;
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }
}
