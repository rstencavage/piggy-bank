package bankapp.dto;

/**
 * JSON response containing a user's balance.
 */
public class BalanceResult {
    public boolean success;
    public String message;
    public double balance;

    public BalanceResult(boolean success, String message, double balance) {
        this.success = success;
        this.message = message;
        this.balance = balance;
    }
}
