package bankapp;

import bankapp.dto.*;
import bankapp.handlers.*;
import bankapp.security.JwtUtil;
import bankapp.security.UnauthorizedException;
import bankapp.security.Auth;
import com.google.gson.Gson;

import java.sql.Connection;
import java.util.Map;

import static spark.Spark.*;

/**
 * Starts an HTTP server and defines REST API endpoints.
 *
 * @author Ryan Stencavage
 */
public class BankServer {
    private static final Gson gson = new Gson();

    /**
     * Application entry point. Configures the server and registers routes.
     */
    public static void main(String[] args) {

        port(5230);                 // HTTP server port
        enableCORS("*", "*", "*");  // Cross-origin request settings

        exception(UnauthorizedException.class, (e, req, res) -> {
            res.status(401);
            res.type("application/json");
            res.body(gson.toJson(Map.of("error", e.getMessage())));
        });

        System.out.println("HTTP BankServer running on http://localhost:5230");

        // Basic status check
        get("/ping", (req, res) -> "BankServer online");

        // Authentication and account routes
        post("/login", (req, res) -> {

            // Parse JSON request body into a LoginRequest object
            LoginRequest data = gson.fromJson(req.body(), LoginRequest.class);

            try (Connection conn = Database.getConnection()) {
                // Perform authentication
                LoginResult result = LoginHandler.authenticate(conn, data.username, data.password);

                // If login successful, create token and attach it
                if (result.success) {
                    result.token = JwtUtil.createToken(data.username);
                }

                // Return JSON result
                res.type("application/json");
                return gson.toJson(result);
            }
        });

        post("/register", (req, res) -> {

            RegisterRequest data = gson.fromJson(req.body(), RegisterRequest.class);

            try (Connection conn = Database.getConnection()) {
                RegisterResult result = RegisterHandler.register(conn, data.username, data.password);

                res.type("application/json");
                return gson.toJson(result);
            }
        });

        // Account operations
        get("/balance", (req, res) -> {
            String username = Auth.requireUsername(req);

            try (Connection conn = Database.getConnection()) {
                BalanceResult result = BalanceHandler.getBalance(conn, username);

                res.type("application/json");
                return gson.toJson(result);
            }
        });

        post("/deposit", (req, res) -> {
            String username = Auth.requireUsername(req);
            DepositRequest data = gson.fromJson(req.body(), DepositRequest.class);

            try (Connection conn = Database.getConnection()) {
                ActionResult result = DepositHandler.deposit(conn, username, data.amount);

                res.type("application/json");
                return gson.toJson(result);
            }
        });

        post("/withdraw", (req, res) -> {
            String username = Auth.requireUsername(req);
            WithdrawRequest data = gson.fromJson(req.body(), WithdrawRequest.class);

            try (Connection conn = Database.getConnection()) {
                ActionResult result = WithdrawHandler.withdraw(conn, username, data.amount);

                res.type("application/json");
                return gson.toJson(result);
            }
        });

        post("/transfer", (req, res) -> {
            // Verify caller is authenticated; the token subject is the sender
            String fromUser = Auth.requireUsername(req);

            // Convert JSON body → TransferRequest object
            TransferRequest data = gson.fromJson(req.body(), TransferRequest.class);

            try (Connection conn = Database.getConnection()) {
                ActionResult result = TransferHandler.transfer(conn, fromUser, data.toUser, data.amount);

                res.type("application/json");
                return gson.toJson(result);
            }
        });

        // Transaction history
        get("/history", (req, res) -> {
            String username = Auth.requireUsername(req);

            try (Connection conn = Database.getConnection()) {
                HistoryResult result = HistoryHandler.history(conn, username);

                res.type("application/json");
                return gson.toJson(result);
            }
        });
    }

    /**
     * Configures Cross-Origin Resource Sharing headers for all requests.
     *
     * @param origin  Allowed origins
     * @param methods Allowed HTTP methods
     * @param headers Allowed request headers
     */
    private static void enableCORS(String origin, String methods, String headers) {

        // Preflight request handler
        options("/*", (req, res) -> {

            String reqHeaders = req.headers("Access-Control-Request-Headers");
            if (reqHeaders != null)
                res.header("Access-Control-Allow-Headers", reqHeaders);

            String reqMethod = req.headers("Access-Control-Request-Method");
            if (reqMethod != null)
                res.header("Access-Control-Allow-Methods", reqMethod);

            return "OK";
        });

        // CORS headers for standard requests
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", origin);
            res.header("Access-Control-Allow-Methods", methods);
            res.header("Access-Control-Allow-Headers", headers);
        });
    }
}
