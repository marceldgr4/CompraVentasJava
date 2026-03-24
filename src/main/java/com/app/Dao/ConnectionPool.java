package com.app.Dao;

import com.app.Config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

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
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");

        dataSource = new HikariDataSource(config);
    }
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    public static void close (){
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
