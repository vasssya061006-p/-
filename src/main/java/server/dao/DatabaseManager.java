package server.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton DatabaseManager for managing JDBC database connections.
 * Implements the Singleton pattern to ensure only one connection pool manager exists.
 * Reads configuration from config.properties file.
 */
public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private Connection connection;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbDriver;
    private boolean initialized = false;
    
    private DatabaseManager() {
        loadConfiguration();
    }
    
    /**
     * Returns the singleton instance of DatabaseManager.
     * Thread-safe implementation using double-checked locking.
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Loads database configuration from config.properties file.
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Warning: config.properties not found. Using defaults.");
                setDefaults();
                return;
            }
            props.load(input);
            dbDriver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            dbUrl = props.getProperty("db.url", "jdbc:mysql://localhost:3306/education_system");
            dbUser = props.getProperty("db.user", "root");
            dbPassword = props.getProperty("db.password", "root");
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            setDefaults();
        }
    }
    
    private void setDefaults() {
        dbDriver = "org.h2.Driver";
        dbUrl = "jdbc:h2:./education_db";
        dbUser = "sa";
        dbPassword = "";
    }
    
    /**
     * Initializes the database driver and creates connection.
     */
    public synchronized void initialize() throws SQLException, ClassNotFoundException {
        if (initialized) return;
        
        Class.forName(dbDriver);
        connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        initialized = true;
        System.out.println("Database connection established: " + dbUrl);
    }
    
    /**
     * Returns the active database connection.
     * Initializes if not already done.
     */
    public synchronized Connection getConnection() throws SQLException, ClassNotFoundException {
        if (!initialized) {
            initialize();
        }
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }
        return connection;
    }
    
    /**
     * Closes the database connection.
     */
    public synchronized void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        initialized = false;
    }
    
    /**
     * Checks if the database is connected.
     */
    public synchronized boolean isConnected() {
        try {
            return initialized && connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Resets the singleton instance (useful for testing).
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
    
    // Getters for configuration
    public String getDbUrl() { return dbUrl; }
    public String getDbUser() { return dbUser; }
}
