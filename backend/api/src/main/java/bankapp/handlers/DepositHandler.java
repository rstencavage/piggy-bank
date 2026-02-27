package bankapp.handlers;

import bankapp.dto.ActionResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handles a DEPOSIT (amount) request.
 *
 * @author Ryan Stencavage
 */
public class DepositHandler {
    /**
     * Deposits into a given username's account.
     *
     * @param conn   active database connection
     * @param username the username to deposit to
     * @param amount amount to deposit
     */
    public static ActionResult deposit(Connection conn, String username, double amount) {

        if (amount <= 0) {
            return new ActionResult(false, "Deposit amount must be positive.");
        }

        String updateBalSql = "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE + ? WHERE CUS_UNAME = ?";
        String insertTxn ="INSERT INTO TRANSACTION_RECORD (CUS_ID_SOURCE, CUS_ID_DEST, TXN_AMOUNT) VALUES (NULL, ?, ?)";

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
            // update balance
            try (PreparedStatement update = conn.prepareStatement(updateBalSql)) {
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
            return new ActionResult(true, "Deposit successful.");

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
