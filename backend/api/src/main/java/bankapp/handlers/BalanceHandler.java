package bankapp.handlers;

import bankapp.dto.BalanceResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles a BALANCE request.
 *
 * @author Ryan Stencavage
 */
public class BalanceHandler {
    /**
     * Returns the balance for a given username
     *
     * @param conn   active database connection
     * @param username the username to return the balance of
     */
    public static BalanceResult getBalance(Connection conn, String username) {

        String sql = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return new BalanceResult(false, "User not found.", 0.00);
            }
            double bal = rs.getDouble("CUS_BALANCE");
            return new BalanceResult(true, "Balance retrieved.", bal);

        } catch (SQLException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return new BalanceResult(false, "Database error.", 0.00);
        }
    }
}
