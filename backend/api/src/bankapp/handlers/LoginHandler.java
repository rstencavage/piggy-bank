package bankapp.handlers;

import bankapp.dto.LoginResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles a LOGIN (username password) request.
 *
 * @author Ryan Stencavage
 */
public class LoginHandler {

    /**
     * Attempts to authenticate a user.
     *
     * @param conn      Active database connection.
     * @param username  Supplied username.
     * @param password  Supplied password.
     * @return A LoginResult object containing success status and messages.
     */
    public static LoginResult authenticate(Connection conn, String username, String password) {

        String sql = "SELECT CUS_PASSWD FROM CUSTOMER WHERE CUS_UNAME = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username); // insert username into the query
            ResultSet rs = ps.executeQuery(); // execute the lookup

            // error if username is not found
            if (!rs.next()) {
                return new LoginResult(false, "Invalid username or password.");
            }

            // compare given password with the stored one
            String storedPass = rs.getString("CUS_PASSWD");

            if (storedPass.equals(password)) {
                return new LoginResult(true, "Login successful.");
            } else {
                return new LoginResult(false, "Invalid username or password.");
            }

        } catch (SQLException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return new LoginResult(false, "Database error.");
        }
    }
}
