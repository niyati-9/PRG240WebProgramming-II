package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Bean
    public DataSource dataSource() {
        logger.info("=== KHEL APP DATABASE CONNECTION INITIALIZATION STARTED ===");
        try {
            Properties props = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
            
            String driverClass = props.getProperty("spring.datasource.driver-class-name");
            String url = props.getProperty("spring.datasource.url");
            String username = props.getProperty("spring.datasource.username");
            
            logger.info("Database configuration loaded:");
            logger.info("Driver: {}", driverClass);
            logger.info("URL: {}", url);
            logger.info("Username: {}", username);
            
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(driverClass);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(props.getProperty("spring.datasource.password"));
            
            // Test the connection
            try (java.sql.Connection connection = dataSource.getConnection()) {
                logger.info("=== KHEL APP DATABASE CONNECTION TEST SUCCESSFUL ===");
                logger.info("Database connection established successfully");
                logger.info("Connection URL: {}", connection.getMetaData().getURL());
                logger.info("Database Product: {}", connection.getMetaData().getDatabaseProductName());
                logger.info("Database Version: {}", connection.getMetaData().getDatabaseProductVersion());
                logger.info("=== KHEL APP DATABASE CONNECTION INITIALIZATION COMPLETED ===");
            } catch (SQLException e) {
                logger.error("=== KHEL APP DATABASE CONNECTION TEST FAILED ===");
                logger.error("Failed to establish database connection: {}", e.getMessage());
                logger.error("SQL State: {}", e.getSQLState());
                logger.error("Error Code: {}", e.getErrorCode());
                throw new RuntimeException("Database connection test failed", e);
            }
            
            return dataSource;
        } catch (IOException e) {
            logger.error("=== KHEL APP DATABASE CONNECTION INITIALIZATION FAILED ===");
            logger.error("Failed to load database properties: {}", e.getMessage());
            logger.error("Error type: {}", e.getClass().getSimpleName());
            logger.error("Stack trace: ", e);
            throw new RuntimeException("Failed to load database properties", e);
        }
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
