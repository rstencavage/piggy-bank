package bankapp.dto;

import java.util.List;

/**
 * Response object for /history API.
 */
public class HistoryResult {
    public boolean success;
    public String message;
    public List<HistoryItem> transactions;

    public HistoryResult(boolean success, String message, List<HistoryItem> transactions) {
        this.success = success;
        this.message = message;
        this.transactions = transactions;
    }
}
