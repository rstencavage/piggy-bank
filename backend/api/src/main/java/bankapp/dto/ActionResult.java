package bankapp.dto;

/**
 * Generic success/error response for simple operations.
 */
public class ActionResult {
    public boolean success;
    public String message;

    public ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
