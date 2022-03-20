package ru.afek.auth.utils;

import com.google.common.collect.Sets;
import net.md_5.bungee.BungeeCord;
import ru.afek.auth.Auth;
import ru.afek.auth.AuthUser;
import ru.afek.bungeecord.SQLConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Afek
 */

public class SQLConnectionAuth {

    private final Auth auth;
    private final SQLConnection sqlConnection;
    private final Logger logger = BungeeCord.getInstance().getLogger();

    public SQLConnectionAuth(Auth auth, SQLConnection sqlConnection) {
        this.auth = auth;
        this.sqlConnection = sqlConnection;
        try {
            createTableAuth();
            createTableIpLimit();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isInvalidName(String name) {
        return (name.contains("'") || name.contains("\""));
    }

    public void saveUserAuth(AuthUser authUser) {
        if (this.sqlConnection.isConnecting() || isInvalidName(authUser.getName()))
            return;
        if (this.sqlConnection.getConnection() != null)
            this.sqlConnection.getExecutor().execute(() -> {
                String sql = "SELECT `Name` FROM `Auth` where `Name` = '" + authUser.getName() + "' LIMIT 1;";
                try (Statement statament = this.sqlConnection.getConnection().createStatement(); ResultSet set = statament.executeQuery(sql)) {
                    if (!set.next()) {
                        sql = "INSERT INTO `Auth` (`Name`, `Password`, `Ip`, `Session`, `Email`) VALUES ('" + authUser.getName() + "','" + authUser.getPassword() + "','" + authUser.getIp() + "','" + String.valueOf(authUser.getSession()) + "','" + authUser.getEmail() + "');";
                        statament.executeUpdate(sql);
                    } else {
                        sql = "UPDATE `Auth` SET `Password` = '" + authUser.getPassword() + "', `Ip` = '" + authUser.getIp() + "', `Session` = '" + String.valueOf(authUser.getSession()) + "', `Email` = '" + authUser.getEmail() + "' where `Name` = '" + authUser.getName() + "';";
                        statament.executeUpdate(sql);
                    }
                } catch (SQLException ignored) {
                    this.sqlConnection.getExecutor().execute(this.sqlConnection::setupConnect);
                }
            });
    }

    private void createTableAuth() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS `Auth` (`Name` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE, `Password` VARCHAR(250) NOT NULL,`Ip` VARCHAR(16) NOT NULL,`Session` BIGINT NOT NULL, `Email` VARCHAR(250) NOT NULL);";
        try (PreparedStatement statement = this.sqlConnection.getConnection().prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private void createTableIpLimit() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS `IpLimit` (`Name` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE,`IpLimitSize` BIGINT NOT NULL);";
        try (PreparedStatement statement = this.sqlConnection.getConnection().prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private void loadUsers() throws SQLException {
        try (PreparedStatement statament = this.sqlConnection.getConnection().prepareStatement("SELECT * FROM `Auth`;");
             ResultSet set = statament.executeQuery()) {
            int i = 0;
            while (set.next()) {
                String name = set.getString("Name");
                String password = set.getString("Password");
                String ip = set.getString("Ip");
                String email = set.getString("Email");
                long session = Long.parseLong(set.getString("Session"));
                AuthUser user = new AuthUser(name, password, ip, session, email);
                this.auth.addUserToCache(user);
                i++;
            }

            this.logger.log(Level.INFO, "[Auth] Данные игроков успешно загружены ({0})", i);
        }

//        try (PreparedStatement statament = this.sqlConnection.getConnection().prepareStatement("SELECT * FROM `IpLimit`;");
//             ResultSet set = statament.executeQuery()) {
//            while (set.next()) {
//                String name = set.getString("Name");
//                int ipLimit = set.getInt("IpLimitSize");
//                if (!this.auth.isRegistered(name)) continue;
//
//                AuthUser user = this.auth.getUser(name);
//                user.setIpLimit(ipLimit);
//            }
//        }
    }

    public void removeUserAuth(String name) {
        if (this.sqlConnection.getConnection() != null)
            this.sqlConnection.getExecutor().execute(() -> {
                try (PreparedStatement statament = this.sqlConnection.getConnection().prepareStatement("DELETE FROM `Auth` WHERE `Name` = '" + name.toLowerCase() + "';")) {
                    statament.execute();
                } catch (SQLException ignored) {
                }
                try (PreparedStatement statament = this.sqlConnection.getConnection().prepareStatement("DELETE FROM `IpLimit` WHERE `Name` = '" + name.toLowerCase() + "';")) {
                    statament.execute();
                } catch (SQLException ignored) {
                }
            });
    }

    public void saveUserIpLimit(String name, int limit) {
        name = name.toLowerCase();
        if (this.sqlConnection.isConnecting())
            return;
        if (this.sqlConnection.getConnection() != null) {
            String finalName = name;
            this.sqlConnection.getExecutor().execute(() -> {
                String sql = "SELECT `Name` FROM `IpLimit` where `Name` = '" + finalName + "' LIMIT 1;";
                try (Statement statament = this.sqlConnection.getConnection().createStatement(); ResultSet set = statament.executeQuery(sql)) {
                    if (!set.next()) {
                        sql = "INSERT INTO `IpLimit` (`Name`, `IpLimitSize`) VALUES ('" + finalName + "','" + limit + "');";
                        statament.executeUpdate(sql);
                    } else {
                        sql = "UPDATE `IpLimit` SET `IpLimitSize` = '" + limit + "' where `Name` = '" + finalName + "';";
                        statament.executeUpdate(sql);
                    }
                } catch (SQLException ignored) {
                    this.sqlConnection.getExecutor().execute(this.sqlConnection::setupConnect);
                }
            });
        }
    }

    public ConcurrentHashMap<String, String> loadUsersIp() {
        ConcurrentHashMap<String, String> similarPlayers = new ConcurrentHashMap<>();
        try (PreparedStatement statament = this.sqlConnection.getConnection().prepareStatement("SELECT * FROM `Auth`;");
             ResultSet set = statament.executeQuery()) {
            while (set.next()) {
                String name = set.getString("Name");
                String ip = set.getString("Ip");
                similarPlayers.put(name, ip);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return similarPlayers;
    }
//
//    public int getUserIpLimit(String name) {
//        int limit = SettingsAuth.IMP.USER_COUNT;
//        ConcurrentHashMap<String, String> similarPlayers = new ConcurrentHashMap<>();
//        try (PreparedStatement statament = this.sqlConnection.getConnection().prepareStatement("SELECT `IpLimitSize` FROM `IpLimit` where `Name` = '" + name + "' LIMIT 1;");
//             ResultSet set = statament.executeQuery()) {
//            if (set.next()) {
//                limit = set.getInt("IpLimitSize");
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return limit;
//    }

    public Set<String> getEqualIp(String name, String ip) {
        ConcurrentHashMap<String, String> playerIps = loadUsersIp();
        playerIps.remove(name);
        Set<String> names = getKeysByValue(playerIps, ip);
        if (names.isEmpty())
            return Sets.newConcurrentHashSet();
        if (names.contains(name.toLowerCase()))
            names.remove(name.toLowerCase());
        return names;
    }

    private Set<String> getKeysByValue(ConcurrentHashMap<String, String> map, String value) {
        Stream<String> string = map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(Map.Entry::getKey);
        return string.collect(Collectors.toSet());
    }
}
