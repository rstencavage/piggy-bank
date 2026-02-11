package bankapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles connection to the local MySQL database.
 *
 * @author Ryan Stencavage
 */
public class Database {
    // Local MySQL URL
    private static final String URL = "jdbc:mysql://localhost:3306/bankdb";

    // MySQL login info
    private static final String USER = "root";
    private static final String PASSWORD = "RyanSql05$";

    /**
     * Opens and returns a new connection to the database. It is called by the handlers when they need to run SQL.
     *
     * @return a fresh Connection object
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database.", e);
        }
    }
}