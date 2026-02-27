package bankapp.handlers;

import bankapp.dto.ActionResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles a TRANSFER (username amount) request.
 *
 * @author Ryan Stencavage
 */
public class TransferHandler {
    /**
     * Transfers money from one user to another.
     */
    public static ActionResult transfer(Connection conn, String fromUser, String toUser, double amount) {

        if (amount <= 0) {
            return new ActionResult(false, "Transfer amount must be positive.");
        }

        if (fromUser.equals(toUser)) {
            return new ActionResult(false, "Cannot transfer to the same user.");
        }

        String updateSourceSql =
                "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE - ? WHERE CUS_UNAME = ? AND CUS_BALANCE >= ?";
        String updateDestSql = "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE + ? WHERE CUS_UNAME = ?";
        String insertTxn = "INSERT INTO TRANSACTION_RECORD (CUS_ID_SOURCE, CUS_ID_DEST, TXN_AMOUNT) VALUES (?, ?, ?)";

        boolean oldAutoCommit;
        try {
            oldAutoCommit = conn.getAutoCommit();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ActionResult(false, "Database error.");
        }

        try {
            conn.setAutoCommit(false);
            String lockSql = "SELECT CUS_UNAME FROM CUSTOMER WHERE CUS_UNAME = ? FOR UPDATE";

            // lock both users in a consistent order to prevent deadlocks
            String first;
            String second;

            if (fromUser.compareTo(toUser) < 0) {
                first = fromUser;
                second = toUser;
            } else {
                first = toUser;
                second = fromUser;
            }

            try (PreparedStatement lock = conn.prepareStatement(lockSql)) {

                lock.setString(1, first);
                try (ResultSet rs = lock.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new ActionResult(false,
                                first.equals(fromUser) ? "Sender not found." : "Recipient not found.");
                    }
                }

                lock.setString(1, second);
                try (ResultSet rs = lock.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new ActionResult(false,
                                second.equals(fromUser) ? "Sender not found." : "Recipient not found.");
                    }
                }
            }

            int rows;

            // withdraw from sender
            try (PreparedStatement withdraw = conn.prepareStatement(updateSourceSql)) {
                withdraw.setDouble(1, amount);
                withdraw.setString(2, fromUser);
                withdraw.setDouble(3, amount);
                rows = withdraw.executeUpdate();
            }

            if (rows == 0) {
                conn.rollback();
                return new ActionResult(false, "Insufficient funds.");
            }

            // deposit to recipient
            try (PreparedStatement deposit = conn.prepareStatement(updateDestSql)) {
                deposit.setDouble(1, amount);
                deposit.setString(2, toUser);
                rows = deposit.executeUpdate();
            }

            if (rows == 0) {
                conn.rollback();
                return new ActionResult(false, "Recipient not found.");
            }

            // log the transaction
            try (PreparedStatement insert = conn.prepareStatement(insertTxn)) {
                insert.setString(1, fromUser);
                insert.setString(2, toUser);
                insert.setDouble(3, amount);
                insert.executeUpdate();
            }
            conn.commit();
            return new ActionResult(true, "Transfer successful.");

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
