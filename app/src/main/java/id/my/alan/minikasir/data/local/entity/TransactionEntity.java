package id.my.alan.minikasir.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a sales transaction in the MiniKasir POS system.
 * Mapped to the "transactions" table in the local SQLite database.
 *
 * <p>A transaction progresses through the following lifecycle:
 * <pre>
 *   PENDING  →  SYNCED
 *            ↘  FAILED
 * </pre>
 */
@Entity(
        tableName = "transactions",
        indices = {
                @Index(value = {"transaction_code"}, unique = true)
        }
)
public class TransactionEntity {

    // -------------------------------------------------------------------------
    // Status constants
    // -------------------------------------------------------------------------

    /**
     * The transaction has been saved locally but has not yet been synced
     * to the remote server.
     */
    public static final String STATUS_PENDING = "PENDING";

    /**
     * The transaction has been successfully synced to the remote server.
     */
    public static final String STATUS_SYNCED = "SYNCED";

    /**
     * The transaction failed to sync after the maximum number of retries.
     */
    public static final String STATUS_FAILED = "FAILED";

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Auto-generated primary key. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    /**
     * Human-readable unique code for the transaction, e.g. "TRX-20260824-001".
     * Enforced unique by a database index.
     */
    @NonNull
    @ColumnInfo(name = "transaction_code")
    private String transactionCode;

    /** Sum of all item subtotals in Rupiah. */
    @ColumnInfo(name = "total_amount")
    private long totalAmount;

    /** Total number of line items in this transaction. */
    @ColumnInfo(name = "item_count")
    private int itemCount;

    /**
     * Current sync status of the transaction.
     * One of {@link #STATUS_PENDING}, {@link #STATUS_SYNCED}, or {@link #STATUS_FAILED}.
     */
    @NonNull
    @ColumnInfo(name = "status")
    private String status;

    /** Optional cashier or customer note attached to this transaction. */
    @Nullable
    @ColumnInfo(name = "note")
    private String note;

    /** Unix timestamp (milliseconds) when this transaction was created. */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    /**
     * Unix timestamp (milliseconds) when this transaction was successfully
     * synced to the server. {@code 0} if not yet synced.
     */
    @ColumnInfo(name = "synced_at")
    private long syncedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default no-arg constructor required by Room.
     */
    public TransactionEntity() {
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.syncedAt = 0L;
    }

    /**
     * Convenience constructor for creating a new transaction.
     *
     * @param transactionCode unique transaction code
     * @param totalAmount     total sale amount in Rupiah
     * @param itemCount       number of distinct product lines
     * @param note            optional note (may be null)
     */
    @Ignore
    public TransactionEntity(
            @NonNull String transactionCode,
            long totalAmount,
            int itemCount,
            @Nullable String note) {
        this.transactionCode = transactionCode;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.note = note;
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.syncedAt = 0L;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(@NonNull String transactionCode) { this.transactionCode = transactionCode; }

    public long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    @NonNull
    public String getStatus() { return status; }
    public void setStatus(@NonNull String status) { this.status = status; }

    @Nullable
    public String getNote() { return note; }
    public void setNote(@Nullable String note) { this.note = note; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getSyncedAt() { return syncedAt; }
    public void setSyncedAt(long syncedAt) { this.syncedAt = syncedAt; }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TransactionEntity{" +
                "id=" + id +
                ", transactionCode='" + transactionCode + '\'' +
                ", totalAmount=" + totalAmount +
                ", itemCount=" + itemCount +
                ", status='" + status + '\'' +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                ", syncedAt=" + syncedAt +
                '}';
    }
}
