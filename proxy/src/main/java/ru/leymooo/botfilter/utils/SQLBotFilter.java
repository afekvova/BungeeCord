package ru.leymooo.botfilter.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.md_5.bungee.BungeeCord;
import ru.afek.bungeecord.SQLConnection;
import ru.leymooo.botfilter.BotFilter;
import ru.leymooo.botfilter.BotFilterUser;
import ru.leymooo.botfilter.config.Settings;

public class SQLBotFilter {

    private final BotFilter botFilter;
    private Connection connection;
    private boolean connecting = false;
    private long nextCleanUp = System.currentTimeMillis() + 7200000L;
    private final ExecutorService executor;
    private final Logger logger = BungeeCord.getInstance().getLogger();
    private SQLConnection sqlConnection;

    public SQLBotFilter(BotFilter botFilter, SQLConnection sqlConnection) {
        this.botFilter = botFilter;
        this.sqlConnection = sqlConnection;
        this.connection = sqlConnection.getConnection();
        this.connecting = sqlConnection.isConnecting();
        this.executor = sqlConnection.getExecutor();
        try {
            createTable();
            alterLastJoinColumn();
            clearOldUsers();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS `Users` (`Name` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE,`Ip` VARCHAR(16) NOT NULL,`LastCheck` BIGINT NOT NULL,`LastJoin` BIGINT NOT NULL);";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private void alterLastJoinColumn() {
        try (ResultSet rs = this.connection.getMetaData().getColumns(null, null, "Users", "LastJoin")) {
            if (!rs.next())
                try (Statement st = this.connection.createStatement()) {
                    st.executeUpdate("ALTER TABLE `Users` ADD COLUMN `LastJoin` BIGINT NOT NULL DEFAULT 0;");
                    st.executeUpdate("UPDATE `Users` SET LastJoin = LastCheck");
                }
        } catch (Exception e) {
            this.logger.log(Level.WARNING, "[BungeeCaptcha] ", e);
        }
    }

    private void clearOldUsers() throws SQLException {
        if (Settings.IMP.SQL.PURGE_TIME <= 0)
            return;
        Calendar calendar = Calendar.getInstance();
        calendar.add(5, -Settings.IMP.SQL.PURGE_TIME);
        long until = calendar.getTimeInMillis();
        try (PreparedStatement statement = this.connection.prepareStatement("SELECT `Name` FROM `Users` WHERE `LastJoin` < " + until + ";")) {
            ResultSet set = statement.executeQuery();
            while (set.next())
                this.botFilter.removeUser(set.getString("Name"));
        }
        if (this.connection != null)
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM `Users` WHERE `LastJoin` < " + until + ";")) {
                this.logger.log(Level.INFO, "[BungeeCaptcha] Очистка аккаунтов. {0} ",
                        statement.executeUpdate());
            }
    }

    private void loadUsers() throws SQLException {
        try (PreparedStatement statament = this.connection.prepareStatement("SELECT * FROM `Users`;");
             ResultSet set = statament.executeQuery()) {
            int i = 0;
            while (set.next()) {
                String name = set.getString("Name");
                String ip = set.getString("Ip");
                if (isInvalidName(name)) {
                    removeUser("REMOVE FROM `Users` WHERE `Ip` = '" + ip + "' AND `LastCheck` = '" + set
                            .getLong("LastCheck") + "';");
                    continue;
                }
                long lastCheck = set.getLong("LastCheck");
                long lastJoin = set.getLong("LastJoin");
                BotFilterUser botFilterUser = new BotFilterUser(name, ip, lastCheck, lastJoin);
                this.botFilter.addUserToCache(botFilterUser);
                i++;
            }
            this.logger.log(Level.INFO, "[BungeeCaptcha] Загружено пользователей капчи ({0})", i);
        }
    }

    private boolean isInvalidName(String name) {
        return (name.contains("'") || name.contains("\""));
    }

    private void removeUser(String sql) {
        if (this.connection != null)
            this.executor.execute(() -> {
                try (PreparedStatement statament = this.connection.prepareStatement(sql)) {
                    statament.execute();
                } catch (SQLException sQLException) {
                }
            });
    }

    public void saveUser(BotFilterUser botFilterUser) {
        if (this.connecting || isInvalidName(botFilterUser.getName()))
            return;
        if (this.connection != null)
            this.executor.execute(() -> {
                long timestamp = System.currentTimeMillis();
                String sql = "SELECT `Name` FROM `Users` where `Name` = '" + botFilterUser.getName() + "' LIMIT 1;";
                try (Statement statament = this.connection.createStatement(); ResultSet set = statament.executeQuery(sql)) {
                    if (!set.next()) {
                        sql = "INSERT INTO `Users` (`Name`, `Ip`, `LastCheck`, `LastJoin`) VALUES ('" + botFilterUser.getName() + "','" + botFilterUser.getIp() + "','" + botFilterUser.getLastCheck() + "','" + botFilterUser.getLastJoin() + "');";
                        statament.executeUpdate(sql);
                    } else {
                        sql = "UPDATE `Users` SET `Ip` = '" + botFilterUser.getIp() + "', `LastCheck` = '" + botFilterUser.getLastCheck() + "', `LastJoin` = '" + botFilterUser.getLastJoin() + "' where `Name` = '" + botFilterUser.getName() + "';";
                        statament.executeUpdate(sql);
                    }
                } catch (SQLException ex) {
                    this.logger.log(Level.WARNING, "[BungeeCaptcha] ", ex);
                    this.logger.log(Level.WARNING, sql);
                }
            });
    }

    public void tryCleanUP() {
        if (Settings.IMP.SQL.PURGE_TIME > 0 && this.nextCleanUp -
                System.currentTimeMillis() <= 0L) {
            this.nextCleanUp = System.currentTimeMillis() + 7200000L;
            try {
                clearOldUsers();
            } catch (SQLException ex) {
                this.sqlConnection.setupConnect();
                this.logger.log(Level.WARNING, "[BungeeCaptcha] Warning:", ex);
            }
        }
    }

    public void close() {
        this.executor.shutdownNow();
        try {
            if (this.connection != null)
                this.connection.close();
        } catch (SQLException sQLException) {
        }
        this.connection = null;
    }
}
