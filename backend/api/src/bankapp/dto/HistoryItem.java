package bankapp.dto;

/**
 * A single transaction entry returned in history results.
 */
public class HistoryItem {

    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAW = "WITHDRAW";
    public static final String TRANSFER_IN = "TRANSFER_IN";
    public static final String TRANSFER_OUT = "TRANSFER_OUT";

    public String type;
    public String fromUser;
    public String toUser;
    public double amount;
    public String time;

    public HistoryItem(String type, String fromUser, String toUser, double amount, String time) {
        this.type = type;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.amount = amount;
        this.time = time;
    }
}
