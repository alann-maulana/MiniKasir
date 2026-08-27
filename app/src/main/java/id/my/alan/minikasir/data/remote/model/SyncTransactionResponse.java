package id.my.alan.minikasir.data.remote.model;

/**
 * Server response payload for a single transaction sync operation.
 *
 * <p>Returned inside an {@link ApiResponse} envelope:
 * <pre>
 * {
 *   "success": true,
 *   "message": "Transaction synced",
 *   "data": {
 *     "transactionCode": "TRX-20260824-001",
 *     "status": "SYNCED",
 *     "syncedAt": 1724504272000,
 *     "message": "Transaction accepted by server"
 *   }
 * }
 * </pre>
 */
public class SyncTransactionResponse {

    /** The transaction code echoed back by the server for correlation. */
    private String transactionCode;

    /**
     * Final status assigned by the server.
     * Typical values: {@code "SYNCED"}, {@code "DUPLICATE"}, {@code "REJECTED"}.
     */
    private String status;

    /** Unix-epoch milliseconds when the server recorded the sync. */
    private long syncedAt;

    /** Optional server message providing additional context. */
    private String message;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** No-arg constructor required by Gson. */
    public SyncTransactionResponse() {
    }

    public SyncTransactionResponse(String transactionCode, String status, long syncedAt, String message) {
        this.transactionCode = transactionCode;
        this.status = status;
        this.syncedAt = syncedAt;
        this.message = message;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(long syncedAt) {
        this.syncedAt = syncedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SyncTransactionResponse{"
                + "transactionCode='" + transactionCode + '\''
                + ", status='" + status + '\''
                + ", syncedAt=" + syncedAt
                + ", message='" + message + '\''
                + '}';
    }
}
