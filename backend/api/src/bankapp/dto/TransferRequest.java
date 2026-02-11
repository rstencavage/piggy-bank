package bankapp.dto;

/**
 * JSON input for a transfer request.
 */
public class TransferRequest {
    public String fromUser;
    public String toUser;
    public double amount;
}
