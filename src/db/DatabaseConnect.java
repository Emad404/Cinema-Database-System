package db;

import java.sql.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConnect {

    private static String url;
    private static String user;
    private static String pass;
    private static boolean isInitialized = false;

    // Private constructor to prevent instantiation
    private DatabaseConnect() {}

    // Initialize connection parameters from properties file
    private static void initializeConnectionProperties() {
        Properties props = new Properties();
        InputStream inputStream = null;

        try {
            // Try 1: Load from classpath root
            inputStream = DatabaseConnect.class.getClassLoader().getResourceAsStream("database.properties");

            // Try 2: Load from config folder in classpath
            if (inputStream == null) {
                inputStream = DatabaseConnect.class.getClassLoader().getResourceAsStream("config/database.properties");
            }

            // Try 3: Load from working directory (config/)
            if (inputStream == null) {
                String configPath = System.getProperty("user.dir") + "/config/database.properties";
                inputStream = new FileInputStream(configPath);
            }

            // Try 4: Load from src/config (if running directly from src)
            if (inputStream == null) {
                inputStream = new FileInputStream("src/config/database.properties");
            }

            // Load and parse
            if (inputStream != null) {
                props.load(inputStream);
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                pass = props.getProperty("db.password");

                String driverClass = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                Class.forName(driverClass);

                System.out.println(" Successfully loaded database.properties");
                isInitialized = true;
            } else {
                System.err.println("Could not find database.properties — using default fallback.");
                setDefaultConnection();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println(" Error loading database properties: " + e.getMessage());
            setDefaultConnection();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                System.err.println(" Error closing file: " + e.getMessage());
            }
        }
    }

    // Fallback method for setting default connection
    private static void setDefaultConnection() {
        url = "jdbc:mysql://127.0.0.1:3306/Cinema";
        user = "root";
        pass = "Emad$mysql99";
        isInitialized = true;
        System.out.println(" Using default DB connection settings.");
    }

    // Get database connection
    public static Connection getConnection() throws SQLException {
        if (!isInitialized) {
            initializeConnectionProperties();
        }
        return DriverManager.getConnection(url, user, pass);
    }

    // Close DB resources
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {
            System.err.println("Error closing ResultSet: " + e.getMessage());
        }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) {
            System.err.println("Error closing Statement: " + e.getMessage());
        }
        try { if (conn != null) conn.close(); } catch (SQLException e) {
            System.err.println("Error closing Connection: " + e.getMessage());
        }
    }
}
