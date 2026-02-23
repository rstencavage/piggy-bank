package bankapp.handlers;

import bankapp.dto.ActionResult;
import bankapp.dto.BalanceResult;
import bankapp.dto.HistoryResult;
import bankapp.dto.LoginResult;
import bankapp.dto.RegisterResult;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all handler classes using an H2 in-memory database.
 * Each test method gets a fresh connection; the schema is rebuilt before each test.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HandlerTest {

    private static Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // H2 in-memory database with MySQL compatibility mode
        conn = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS TRANSACTION_RECORD");
            st.execute("DROP TABLE IF EXISTS CUSTOMER");
            st.execute("CREATE TABLE CUSTOMER (" +
                    "  CUS_UNAME VARCHAR(32) PRIMARY KEY," +
                    "  CUS_PASSWD_HASH VARCHAR(255) NOT NULL," +
                    "  CUS_BALANCE DECIMAL(15,2) NOT NULL DEFAULT 0.00)");
            st.execute("CREATE TABLE TRANSACTION_RECORD (" +
                    "  TXN_ID INT PRIMARY KEY AUTO_INCREMENT," +
                    "  CUS_ID_SOURCE VARCHAR(32) NULL," +
                    "  CUS_ID_DEST   VARCHAR(32) NULL," +
                    "  TXN_AMOUNT    DECIMAL(15,2) NOT NULL," +
                    "  TXN_DATETIME  DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (CUS_ID_SOURCE) REFERENCES CUSTOMER(CUS_UNAME)," +
                    "  FOREIGN KEY (CUS_ID_DEST)   REFERENCES CUSTOMER(CUS_UNAME))");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.close();
    }

    // ── RegisterHandler ──────────────────────────────────────────────────────

    @Test
    void register_success() throws Exception {
        RegisterResult r = RegisterHandler.register(conn, "alice", "pass1");
        assertTrue(r.success);
    }

    @Test
    void register_duplicateUsername() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        RegisterResult r = RegisterHandler.register(conn, "alice", "pass2");
        assertFalse(r.success);
        assertTrue(r.message.toLowerCase().contains("taken"));
    }

    @Test
    void register_shortUsername() {
        RegisterResult r = RegisterHandler.register(conn, "ab", "pass1");
        assertFalse(r.success);
    }

    @Test
    void register_shortPassword() {
        RegisterResult r = RegisterHandler.register(conn, "alice", "abc");
        assertFalse(r.success);
    }

    @Test
    void register_nullFields() {
        assertFalse(RegisterHandler.register(conn, null, "pass").success);
        assertFalse(RegisterHandler.register(conn, "alice", null).success);
    }

    // ── LoginHandler ─────────────────────────────────────────────────────────

    @Test
    void login_success() throws Exception {
        RegisterHandler.register(conn, "alice", "password");
        LoginResult r = LoginHandler.authenticate(conn, "alice", "password");
        assertTrue(r.success);
    }

    @Test
    void login_wrongPassword() throws Exception {
        RegisterHandler.register(conn, "alice", "password");
        LoginResult r = LoginHandler.authenticate(conn, "alice", "wrong");
        assertFalse(r.success);
    }

    @Test
    void login_unknownUser() {
        LoginResult r = LoginHandler.authenticate(conn, "nobody", "pass");
        assertFalse(r.success);
    }

    // ── BalanceHandler ───────────────────────────────────────────────────────

    @Test
    void balance_newUser_isZero() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        BalanceResult r = BalanceHandler.getBalance(conn, "alice");
        assertTrue(r.success);
        assertEquals(0.00, r.balance, 0.001);
    }

    @Test
    void balance_unknownUser() {
        BalanceResult r = BalanceHandler.getBalance(conn, "nobody");
        assertFalse(r.success);
    }

    // ── DepositHandler ───────────────────────────────────────────────────────

    @Test
    void deposit_success() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        ActionResult r = DepositHandler.deposit(conn, "alice", 100.00);
        assertTrue(r.success);
        assertEquals(100.00, BalanceHandler.getBalance(conn, "alice").balance, 0.001);
    }

    @Test
    void deposit_negativeAmount() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        ActionResult r = DepositHandler.deposit(conn, "alice", -50.00);
        assertFalse(r.success);
    }

    @Test
    void deposit_zeroAmount() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        ActionResult r = DepositHandler.deposit(conn, "alice", 0.00);
        assertFalse(r.success);
    }

    @Test
    void deposit_unknownUser() {
        ActionResult r = DepositHandler.deposit(conn, "nobody", 50.00);
        assertFalse(r.success);
    }

    // ── WithdrawHandler ──────────────────────────────────────────────────────

    @Test
    void withdraw_success() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        DepositHandler.deposit(conn, "alice", 200.00);
        ActionResult r = WithdrawHandler.withdraw(conn, "alice", 50.00);
        assertTrue(r.success);
        assertEquals(150.00, BalanceHandler.getBalance(conn, "alice").balance, 0.001);
    }

    @Test
    void withdraw_insufficientFunds() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        DepositHandler.deposit(conn, "alice", 30.00);
        ActionResult r = WithdrawHandler.withdraw(conn, "alice", 100.00);
        assertFalse(r.success);
        assertTrue(r.message.toLowerCase().contains("insufficient"));
    }

    @Test
    void withdraw_negativeAmount() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        ActionResult r = WithdrawHandler.withdraw(conn, "alice", -10.00);
        assertFalse(r.success);
    }

    @Test
    void withdraw_unknownUser() {
        ActionResult r = WithdrawHandler.withdraw(conn, "nobody", 10.00);
        assertFalse(r.success);
    }

    // ── TransferHandler ──────────────────────────────────────────────────────

    @Test
    void transfer_success() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        RegisterHandler.register(conn, "bob",   "pass2");
        DepositHandler.deposit(conn, "alice", 200.00);
        ActionResult r = TransferHandler.transfer(conn, "alice", "bob", 75.00);
        assertTrue(r.success);
        assertEquals(125.00, BalanceHandler.getBalance(conn, "alice").balance, 0.001);
        assertEquals(75.00,  BalanceHandler.getBalance(conn, "bob").balance, 0.001);
    }

    @Test
    void transfer_insufficientFunds() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        RegisterHandler.register(conn, "bob",   "pass2");
        DepositHandler.deposit(conn, "alice", 10.00);
        ActionResult r = TransferHandler.transfer(conn, "alice", "bob", 100.00);
        assertFalse(r.success);
    }

    @Test
    void transfer_toSameUser() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        ActionResult r = TransferHandler.transfer(conn, "alice", "alice", 50.00);
        assertFalse(r.success);
    }

    @Test
    void transfer_unknownSender() throws Exception {
        RegisterHandler.register(conn, "bob", "pass2");
        ActionResult r = TransferHandler.transfer(conn, "nobody", "bob", 10.00);
        assertFalse(r.success);
    }

    @Test
    void transfer_unknownRecipient() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        DepositHandler.deposit(conn, "alice", 100.00);
        ActionResult r = TransferHandler.transfer(conn, "alice", "nobody", 10.00);
        assertFalse(r.success);
    }

    // ── HistoryHandler ───────────────────────────────────────────────────────

    @Test
    void history_empty() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        HistoryResult r = HistoryHandler.history(conn, "alice");
        assertTrue(r.success);
        assertTrue(r.transactions.isEmpty());
    }

    @Test
    void history_recordsDeposit() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        DepositHandler.deposit(conn, "alice", 100.00);
        HistoryResult r = HistoryHandler.history(conn, "alice");
        assertTrue(r.success);
        assertEquals(1, r.transactions.size());
        assertEquals("DEPOSIT", r.transactions.get(0).type);
    }

    @Test
    void history_recordsWithdrawal() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        DepositHandler.deposit(conn, "alice", 100.00);
        WithdrawHandler.withdraw(conn, "alice", 40.00);
        HistoryResult r = HistoryHandler.history(conn, "alice");
        assertEquals(2, r.transactions.size());
        assertEquals("WITHDRAW", r.transactions.get(1).type);
    }

    @Test
    void history_recordsTransfer() throws Exception {
        RegisterHandler.register(conn, "alice", "pass1");
        RegisterHandler.register(conn, "bob",   "pass2");
        DepositHandler.deposit(conn, "alice", 200.00);
        TransferHandler.transfer(conn, "alice", "bob", 50.00);

        HistoryResult aliceHistory = HistoryHandler.history(conn, "alice");
        HistoryResult bobHistory   = HistoryHandler.history(conn, "bob");

        assertTrue(aliceHistory.transactions.stream()
                .anyMatch(t -> "TRANSFER_OUT".equals(t.type)));
        assertTrue(bobHistory.transactions.stream()
                .anyMatch(t -> "TRANSFER_IN".equals(t.type)));
    }

    @Test
    void history_unknownUser() {
        HistoryResult r = HistoryHandler.history(conn, "nobody");
        assertFalse(r.success);
    }
}
