package bankapp;

import bankapp.handlers.DepositHandler;
import bankapp.dto.ActionResult;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

public class DepositHandlerTest {

    @Test
    void deposit_increasesBalance_andLogsTransaction() throws Exception {
        try (Connection conn = TestDb.newConnectionAndInit()) {

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO CUSTOMER (CUS_UNAME, CUS_PASSWD_HASH, CUS_BALANCE) VALUES (?, ?, ?)"
            )) {
                ps.setString(1, "ryan");
                ps.setString(2, "hash");
                ps.setDouble(3, 100.00);
                ps.executeUpdate();
            }

            ActionResult result = DepositHandler.deposit(conn, "ryan", 25.00);

            assertTrue(result.success);
            assertEquals("Deposit successful.", result.message);

            double bal;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ?"
            )) {
                ps.setString(1, "ryan");
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    bal = rs.getDouble(1);
                }
            }
            assertEquals(125.00, bal, 0.0001);

            int txnCount;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM TRANSACTION_RECORD WHERE CUS_ID_DEST = ? AND TXN_AMOUNT = ?"
            )) {
                ps.setString(1, "ryan");
                ps.setDouble(2, 25.00);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    txnCount = rs.getInt(1);
                }
            }
            assertEquals(1, txnCount);
        }
    }

    @Test
    void deposit_rejectsNonPositiveAmount() throws Exception {
        try (Connection conn = TestDb.newConnectionAndInit()) {
            ActionResult result = DepositHandler.deposit(conn, "ryan", 0);
            assertFalse(result.success);
            assertEquals("Deposit amount must be positive.", result.message);
        }
    }

    @Test
    void deposit_returnsUserNotFound() throws Exception {
        try (Connection conn = TestDb.newConnectionAndInit()) {
            ActionResult result = DepositHandler.deposit(conn, "nope", 10);
            assertFalse(result.success);
            assertEquals("User not found.", result.message);
        }
    }
}
