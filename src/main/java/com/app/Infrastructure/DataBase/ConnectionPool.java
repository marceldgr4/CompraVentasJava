package com.app.Infrastructure.DataBase;

import com.app.Config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

// Archivo tocado para forzar recompilación del IDE
public class ConnectionPool {
    private  static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(AppConfig.get("DB_URL"));
        config.setUsername(AppConfig.get("DB_USERNAME"));
        config.setPassword(AppConfig.get("DB_PASSWORD"));
        config.setMaximumPoolSize(Integer.parseInt(AppConfig.get("DB_POOL_SIZE")));
        config.setPoolName(AppConfig.get("DB_POOL_NAME"));

        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.addDataSourceProperty("cachePrepStmts", "false");
        config.addDataSourceProperty("prepStmtCacheSize", "0");

        dataSource = new HikariDataSource(config);
    }
    private static boolean migrated = false;

    public static synchronized Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        if (!migrated) {
            migrated = true;
            DatabaseMigrator.verifyAndMigrate(conn);
        }
        return conn;
    }
    public static void close (){
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
