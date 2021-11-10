package ru.afek.bungeecord;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import ru.leymooo.botfilter.config.Settings;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLConnection {

    @Setter
    @Getter
    private Connection connection;
    @Getter
    private boolean connecting;
    @Getter
    private final ExecutorService executor;
    @Getter
    private final Logger logger;

    public SQLConnection() {
        this.connecting = false;
        this.executor = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("BungeeCord-SQL-%d").build());
        this.logger = BungeeCord.getInstance().getLogger();
        this.setupConnect();
    }

    public void setupConnect()
    {

        try
        {
            connecting = true;
            if ( executor.isShutdown() )
            {
                return;
            }
            if ( connection != null && connection.isValid( 3 ) )
            {
                return;
            }
            logger.info( "[BotFilter] Подключаюсь к датабазе..." );
            long start = System.currentTimeMillis();
            File file = new File("BotFilter", "database.db");
            if(file.getParentFile() != null && !file.getParentFile().exists())
                file.getParentFile().mkdirs();

            if ( Settings.IMP.SQL.STORAGE_TYPE.equalsIgnoreCase( "mysql" ) )
            {
                Settings.SQL s = Settings.IMP.SQL;
                connectToDatabase( String.format( "JDBC:mysql://%s:%s/%s?useSSL=false&useUnicode=true&characterEncoding=utf-8", s.HOSTNAME, String.valueOf( s.PORT ), s.DATABASE ), s.USER, s.PASSWORD );
            } else
            {
                Class.forName( "org.sqlite.JDBC" );
                connectToDatabase( "JDBC:sqlite:BotFilter/database.db", null, null );
            }
            logger.log( Level.INFO, "[BotFilter] Подключено ({0} мс)", System.currentTimeMillis() - start );
        } catch ( SQLException | ClassNotFoundException e )
        {
            logger.log( Level.WARNING, "Can not connect to database or execute sql: ", e );
            connection = null;
        } finally
        {
            connecting = false;
        }
    }

    private void connectToDatabase(String url, String user, String password) throws SQLException
    {
        this.connection = DriverManager.getConnection( url, user, password );
    }


    public void close()
    {
        this.executor.shutdownNow();
        try
        {
            if ( connection != null )
            {
                this.connection.close();
            }
        } catch ( SQLException ignore )
        {
        }
        this.connection = null;
    }
}
