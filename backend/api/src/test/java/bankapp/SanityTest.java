package bankapp;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SanityTest {

    @Test
    void h2StartsAndSchemaLoads() throws Exception {
        try (Connection conn = TestDb.newConnectionAndInit()) {
            assertNotNull(conn);
        }
    }
}
