package bankapp.handlers;

import bankapp.dto.ActionResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles a WITHDRAW (amount) request.
 *
 * @author Ryan Stencavage
 */
public class WithdrawHandler {

    /**
     * Withdraws from a given username's account.
     *
     * @param conn   active database connection
     * @param username the username to withdraw from
     * @param amount amount to withdraw
     */
    public static ActionResult withdraw(Connection conn, String username, double amount) {
        if (amount <= 0) {
            return new ActionResult(false, "Withdrawal amount must be positive.");
        }

        String updateSql = "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE - ? WHERE CUS_UNAME = ?";
        String insertTxn ="INSERT INTO TRANSACTION_RECORD (CUS_ID_SOURCE, CUS_ID_DEST, TXN_AMOUNT) VALUES (?, NULL, ?)";

        boolean oldAutoCommit;
        try {
            oldAutoCommit = conn.getAutoCommit();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ActionResult(false, "Database error.");
        }

        try {
            conn.setAutoCommit(false);

            int rows;
            double balance = 0.00;
            String balSQL = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ? FOR UPDATE";

            // gets balance and checks if withdrawal amount is greater than balance
            try (PreparedStatement ps = conn.prepareStatement(balSQL)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return new ActionResult(false, "User not found.");
                }
                balance = rs.getDouble("CUS_BALANCE");

            }
            // checks for withdrawal greater than balance
            if (amount > balance) {
                conn.rollback();
                return new ActionResult(false, "Insufficient funds.");
            }

            // update balance
            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setDouble(1, amount);
                update.setString(2, username);
                rows = update.executeUpdate();
            }

            // if no rows updated then username does not exist
            if (rows == 0) {
                conn.rollback();
                return new ActionResult(false, "User not found.");
            }


            // log the transaction
            try (PreparedStatement insert = conn.prepareStatement(insertTxn)) {
                insert.setString(1, username);
                insert.setDouble(2, amount);
                insert.executeUpdate();
            }
            conn.commit();
            return new ActionResult(true, "Withdrawal successful.");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackErr) {
                rollbackErr.printStackTrace();
            }
            e.printStackTrace();
            return new ActionResult(false, "Database error.");

        } finally {
            try {
                conn.setAutoCommit(oldAutoCommit); // restore previous setting
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
