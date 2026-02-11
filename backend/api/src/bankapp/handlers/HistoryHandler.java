package bankapp.handlers;

import bankapp.dto.HistoryItem;
import bankapp.dto.HistoryResult;

import java.sql.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Retrieves transaction history for a given username using the same SQL
 * and formatting as the original BankTask version.
 */
public class HistoryHandler {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
    private static final NumberFormat MONEY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

    /**
     * Gets the transaction history for a user.
     *
     * @param conn     active database connection
     * @param username username whose history is requested
     * @return ActionResult containing success flag and history text
     */
    public static HistoryResult history(Connection conn, String username) {

        // Original history SQL from your BankTask version
        String txnRecordSQL = "SELECT TXN_ID, CUS_ID_SOURCE, CUS_ID_DEST, TXN_AMOUNT, TXN_DATETIME FROM " + "TRANSACTION_RECORD " + "WHERE CUS_ID_SOURCE = ? OR CUS_ID_DEST = ? ORDER BY TXN_ID";

        // Ensures the username exists
        String checkUser = "SELECT 1 FROM CUSTOMER WHERE CUS_UNAME = ?";

        try {
            // Verify user exists
            try (PreparedStatement check = conn.prepareStatement(checkUser)) {
                check.setString(1, username);
                ResultSet rs = check.executeQuery();
                if (!rs.next()) {
                    return new HistoryResult(false, "User not found.", new ArrayList<>());
                }
            }

            List<HistoryItem> history = new ArrayList<>();

            // Execute the history SQL
            try (PreparedStatement ps = conn.prepareStatement(txnRecordSQL)) {
                ps.setString(1, username); // match source
                ps.setString(2, username); // match destination
                ResultSet rs = ps.executeQuery();

                // Read each transaction row
                while (rs.next()) {
                    int id = rs.getInt("TXN_ID");
                    String src = rs.getString("CUS_ID_SOURCE");
                    String dest = rs.getString("CUS_ID_DEST");
                    double amt = rs.getDouble("TXN_AMOUNT");
                    Timestamp ts = rs.getTimestamp("TXN_DATETIME");
                    String type;

                    if (src == null) {
                        type = HistoryItem.DEPOSIT;
                    } else if (dest == null) {
                        type = HistoryItem.WITHDRAW;
                    } else if (dest.equals(username)) {
                        type = HistoryItem.TRANSFER_IN;
                    } else {
                        type = HistoryItem.TRANSFER_OUT;
                    }

                    history.add(new HistoryItem(type, src, dest, amt, ts.toLocalDateTime().format(DATE_FORMATTER)));
                }
            }

            return new HistoryResult(true, "History retrieved.", history);

        } catch (SQLException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return new HistoryResult(false, "Database error.", new ArrayList<>());
        }
    }
}
