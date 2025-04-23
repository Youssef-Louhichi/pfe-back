package com.example.demo.querydsl;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.OracleTemplates;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import javax.sql.DataSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Component;

@Component
public class QueryDSLFactory {
	
	

    public SQLQueryFactory createSQLQueryFactory(String url, String username, String password, String driver) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driver);

        SQLTemplates templates = getSQLTemplates(driver);
        Configuration configuration = new Configuration(templates);

        Supplier<Connection> connectionSupplier = () -> {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to obtain database connection", e);
            }
        };

        return new SQLQueryFactory(configuration, connectionSupplier::get);
    }

    private SQLTemplates getSQLTemplates(String driver) {
        if (driver.contains("mysql")) {
            return new MySQLTemplates();
        } else if (driver.contains("postgresql")) {
            return new PostgreSQLTemplates();
        } else if (driver.contains("oracle")) {
            return new OracleTemplates();
        } else {
            throw new IllegalArgumentException("Unsupported database driver: " + driver);
        }
    }
}