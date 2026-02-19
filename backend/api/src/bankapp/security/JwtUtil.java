package bankapp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Instant;
import java.util.Date;

/**
 * Utility class for creating and verifying JSON Web Tokens used for authentication.
 * The token stores the username in the subject claim and is signed using a secret
 * loaded from the environment. Tokens automatically expire after the configured lifetime.
 */
public final class JwtUtil {

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private static final String SECRET = requireEnv("JWT_SECRET");

    // Token lifetime in seconds (default 60 minutes)
    private static final long TTL_SECONDS = readLongOrDefault("JWT_TTL_MINUTES", 60) * 60L;

    private static final Algorithm ALG = Algorithm.HMAC256(SECRET);

    // Verifier checks signature and expiration automatically
    private static final JWTVerifier VERIFIER = JWT.require(ALG).build();

    private JwtUtil() {}

    /**
     * Creates a signed JWT containing the given username.
     * The token includes issued and expiration timestamps.
     */
    public static String createToken(String username) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(TTL_SECONDS);

        return JWT.create().withSubject(username).withIssuedAt(Date.from(now)).withExpiresAt(Date.from(exp)).sign(ALG);
    }

    /**
     * Verifies a JWT and returns the username stored in the subject claim.
     * Throws JwtAuthException if the token is invalid or expired.
     */
    public static String verifyAndGetUsername(String token) {
        try {
            DecodedJWT jwt = VERIFIER.verify(token);
            String username = jwt.getSubject();

            if (username == null || username.isBlank()) {
                throw new JwtAuthException("Token missing subject");
            }

            return username;

        } catch (JWTVerificationException e) {
            throw new JwtAuthException("Invalid or expired token", e);
        }
    }

    /**
     * Exception thrown when token verification fails.
     */
    public static class JwtAuthException extends RuntimeException {
        public JwtAuthException(String message) {
            super(message);
        }

        public JwtAuthException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Reads a required environment variable from .env or system env.
    // Throws if missing because JWT_SECRET must always exist.
    private static String requireEnv(String key) {
        String value = DOTENV.get(key);
        if (value == null) value = System.getenv(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }

        return value;
    }

    // Reads an optional numeric environment variable.
    // Returns the default value if missing or invalid.
    private static long readLongOrDefault(String key, long def) {
        String value = DOTENV.get(key);
        if (value == null) value = System.getenv(key);

        if (value == null || value.isBlank()) {
            return def;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
