package bankapp.handlers;

import bankapp.dto.RegisterResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Handles a REGISTER (username password) request.
 *
 * @author Ryan Stencavage
 */
public class RegisterHandler {
    /**
     * Attempts to register a new user.
     *
     * @param conn      Active database connection.
     * @param username  Supplied username.
     * @param password  Supplied password.
     * @return A RegisterResult object containing success status and messages.
     */
    public static RegisterResult register(Connection conn, String username, String password) {
        if (username == null || password == null) {
            return new RegisterResult(false, "Username and password are required.");
        }

        // trim whitespace off edges
        username = username.trim();
        password = password.trim();

        if (username.isEmpty() || password.isEmpty()) {
            return new RegisterResult(false, "Username and password are required.");
        }

        if (username.length() < 3) {
            return new RegisterResult(false, "Username must be at least 3 characters.");
        }

        if (password.length() < 4) {
            return new RegisterResult(false, "Password must be at least 4 characters.");
        }

        String checkSQL = "SELECT CUS_PASSWD_HASH FROM CUSTOMER WHERE CUS_UNAME = ?";
        String insertSQL = "INSERT INTO CUSTOMER (CUS_UNAME, CUS_PASSWD_HASH, CUS_BALANCE) VALUES (?, ?, 0.0)";

        try {
            // Check if username already exists
            try (PreparedStatement check = conn.prepareStatement(checkSQL)) {
                check.setString(1, username); // insert username into the query
                ResultSet rs = check.executeQuery(); // execute the lookup

                if (rs.next()) {
                    return new RegisterResult(false, "Username already taken.");
                }
            }

            // Insert the new customer record
            try (PreparedStatement insert = conn.prepareStatement(insertSQL)) {
                insert.setString(1, username);

                // Hash the password using bcrypt to prevent plaintext passwords from being stored in the database.
                String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
                insert.setString(2, hash);
                insert.executeUpdate();
            }

            return new RegisterResult(true, "Registration successful. \nYou can now log in.");

        } catch (SQLException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return new RegisterResult(false, "Database error.");

        }
    }

}
