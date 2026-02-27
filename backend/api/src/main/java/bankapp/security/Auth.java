package bankapp.security;

import spark.Request;

/**
 * Handles authentication of HTTP requests using JWT tokens. Extracts the token from the Authorization header,
 * verifies it, and returns the associated username.
 * Throws UnauthorizedException if the token is missing or invalid.
 *
 * @author Ryan Stencavage
 */
public class Auth {

    /**
     * Extracts and verifies the JWT token from the request Authorization header.
     *
     * @param req The incoming HTTP request
     * @return The username stored in the verified JWT token
     * @throws UnauthorizedException if the Authorization header or token is invalid
     */
    public static String requireUsername(Request req) {

        String header = req.headers("Authorization");

        // Authorization header must exist and begin with "Bearer "
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        // Extract token portion after "Bearer "
        String token = header.substring("Bearer ".length()).trim();

        if (token.isEmpty()) {
            throw new UnauthorizedException("Missing token");
        }

        // Verify token and return associated username
        return JwtUtil.verifyAndGetUsername(token);
    }
}
