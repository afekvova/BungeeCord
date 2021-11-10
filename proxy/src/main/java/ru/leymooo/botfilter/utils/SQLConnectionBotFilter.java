package ru.leymooo.botfilter.utils;

import net.md_5.bungee.BungeeCord;
import ru.afek.bungeecord.SQLConnection;
import ru.leymooo.botfilter.BotFilter;
import ru.leymooo.botfilter.BotFilterUser;
import ru.leymooo.botfilter.config.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Leymooo | Afek
 */
public class SQLConnectionBotFilter
{

    private final BotFilter botFilter;
    private final SQLConnection sqlConnection;
    private long nextCleanUp = System.currentTimeMillis() + ( 60000 * 60 * 2 ); // + 2 hours
    private final Logger logger = BungeeCord.getInstance().getLogger();

    public SQLConnectionBotFilter(BotFilter botFilter, SQLConnection sqlConnection) {
        this.botFilter = botFilter;
        this.sqlConnection = sqlConnection;
        try {
            createTable();
            alterLastJoinColumn();
            clearOldUsers();
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void createTable() throws SQLException
    {
        String sql = "CREATE TABLE IF NOT EXISTS `Users` ("
                + "`Name` VARCHAR(16) NOT NULL PRIMARY KEY UNIQUE,"
                + "`Ip` VARCHAR(16) NOT NULL,"
                + "`LastCheck` BIGINT NOT NULL,"
                + "`LastJoin` BIGINT NOT NULL);";

        try ( PreparedStatement statement = sqlConnection.getConnection().prepareStatement( sql ) )
        {
            statement.executeUpdate();
        }
    }

    private void alterLastJoinColumn()
    {
        try ( ResultSet rs = sqlConnection.getConnection().getMetaData().getColumns( null, null, "Users", "LastJoin" ) )
        {
            if ( !rs.next() )
            {
                try ( Statement st = sqlConnection.getConnection().createStatement() )
                {
                    st.executeUpdate( "ALTER TABLE `Users` ADD COLUMN `LastJoin` BIGINT NOT NULL DEFAULT 0;" );
                    st.executeUpdate( "UPDATE `Users` SET LastJoin = LastCheck" );
                }
            }
        } catch ( Exception e )
        {
            logger.log( Level.WARNING, "[BotFilter] Ошибка при добавлении столбца в таблицу", e );
        }
    }

    private void clearOldUsers() throws SQLException
    {
        if ( Settings.IMP.SQL.PURGE_TIME <= 0 )
        {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add( Calendar.DATE, -Settings.IMP.SQL.PURGE_TIME );
        long until = calendar.getTimeInMillis();
        try ( PreparedStatement statement = sqlConnection.getConnection().prepareStatement( "SELECT `Name` FROM `Users` WHERE `LastJoin` < " + until + ";" ) )
        {
            ResultSet set = statement.executeQuery();
            while ( set.next() )
            {
                botFilter.removeUser( set.getString( "Name" ) );
            }
        }
        if ( this.sqlConnection.getConnection() != null )
        {
            try ( PreparedStatement statement = sqlConnection.getConnection().prepareStatement( "DELETE FROM `Users` WHERE `LastJoin` < " + until + ";" ) )
            {
                logger.log( Level.INFO, "[BotFilter] Очищено {0} аккаунтов", statement.executeUpdate() );
            }
        }
    }

    private void loadUsers() throws SQLException
    {
        try ( PreparedStatement statament = sqlConnection.getConnection().prepareStatement( "SELECT * FROM `Users`;" );
                ResultSet set = statament.executeQuery() )
        {
            int i = 0;
            while ( set.next() )
            {
                String name = set.getString( "Name" );
                String ip = set.getString( "Ip" );
                if ( isInvalidName( name ) )
                {
                    removeUser( "REMOVE FROM `Users` WHERE `Ip` = '" + ip + "' AND `LastCheck` = '" + set.getLong( "LastCheck" ) + "';" );
                    continue;
                }
                long lastCheck = set.getLong( "LastCheck" );
                long lastJoin = set.getLong( "LastJoin" );
                BotFilterUser botFilterUser = new BotFilterUser( name, ip, lastCheck, lastJoin );
                botFilter.addUserToCache( botFilterUser );
                i++;
            }
            logger.log( Level.INFO, "[BotFilter] Белый список игроков успешно загружен ({0})", i );
        }
    }

    private boolean isInvalidName(String name)
    {
        return name.contains( "'" ) || name.contains( "\"" );
    }

    private void removeUser(String sql)
    {
        if ( sqlConnection.getConnection() != null )
        {
            sqlConnection.getExecutor().execute( () ->
            {
                try ( PreparedStatement statament = sqlConnection.getConnection().prepareStatement( sql ) )
                {
                    statament.execute();
                } catch ( SQLException ignored )
                {
                }
            } );
        }
    }

    public void saveUser(BotFilterUser botFilterUser)
    {
        if (isInvalidName( botFilterUser.getName() ) )
            return;
        if ( sqlConnection.getConnection() != null )
        {
            this.sqlConnection.getExecutor().execute( () ->
            {
                final long timestamp = System.currentTimeMillis();
                String sql = "SELECT `Name` FROM `Users` where `Name` = '" + botFilterUser.getName() + "' LIMIT 1;";
                try ( Statement statament = sqlConnection.getConnection().createStatement();
                        ResultSet set = statament.executeQuery( sql ) )
                {
                    if ( !set.next() )
                    {
                        sql = "INSERT INTO `Users` (`Name`, `Ip`, `LastCheck`, `LastJoin`) VALUES "
                            + "('" + botFilterUser.getName() + "','" + botFilterUser.getIp() + "',"
                            + "'" + botFilterUser.getLastCheck() + "','" + botFilterUser.getLastJoin() + "');";
                        statament.executeUpdate( sql );
                    } else
                    {
                        sql = "UPDATE `Users` SET `Ip` = '" + botFilterUser.getIp() + "', `LastCheck` = '" + botFilterUser.getLastCheck() + "',"
                            + " `LastJoin` = '" + botFilterUser.getLastJoin() + "' where `Name` = '" + botFilterUser.getName() + "';";
                        statament.executeUpdate( sql );
                    }
                } catch ( SQLException ex )
                {
                    logger.log( Level.WARNING, "[BotFilter] Не могу выполнить запрос к базе данных", ex );
                    logger.log( Level.WARNING, sql );
                    sqlConnection.getExecutor().execute( () -> this.sqlConnection.setupConnect() );
                }
            } );
        }
    }

    public void tryCleanUP()
    {
        if ( Settings.IMP.SQL.PURGE_TIME > 0 && nextCleanUp - System.currentTimeMillis() <= 0 )
        {
            nextCleanUp = System.currentTimeMillis() + ( 60000 * 60 * 2 ); // + 2 hours
            try
            {
                clearOldUsers();
            } catch ( SQLException ex )
            {
                this.sqlConnection.setupConnect();
                logger.log( Level.WARNING, "[BotFilter] Не могу очистить пользователей", ex );
            }
        }
    }
}
