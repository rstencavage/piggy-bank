package bankapp;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DatabaseEnvTest {

    @Test
    void databaseGetConnectionUsesEnvTest() {
        try (Connection conn = Database.getConnection()) {
            assertNotNull(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}