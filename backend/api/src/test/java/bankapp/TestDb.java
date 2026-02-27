package bankapp;

import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

public class TestDb {

    public static Connection newConnectionAndInit() throws Exception {
        // MySQL mode helps with syntax compatibility
        String url = "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1";
        Connection conn = DriverManager.getConnection(url, "sa", "");

        try (var reader = new InputStreamReader(
                Objects.requireNonNull(TestDb.class.getClassLoader().getResourceAsStream("schema.sql")),
                StandardCharsets.UTF_8
        )) {
            RunScript.execute(conn, reader);
        }

        return conn;
    }
}