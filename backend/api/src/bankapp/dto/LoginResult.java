package bankapp.dto;

/**
 * Represents the result of a login attempt.
 */
public class LoginResult {
    public boolean success;
    public String message;

    public LoginResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
