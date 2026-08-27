package id.my.alan.minikasir.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room entity representing an entry in the offline-first sync queue (outbox pattern).
 * Mapped to the "sync_queue" table in the local SQLite database.
 *
 * <p>When an operation (e.g. a new transaction) is created while the device is offline,
 * a corresponding {@code SyncQueueEntity} is enqueued. A background WorkManager job
 * periodically drains the queue by sending each pending item to the remote API.
 *
 * <p>Lifecycle of a queue item:
 * <pre>
 *   PENDING → PROCESSING → SUCCESS
 *                        ↘ FAILED  (after max retries)
 * </pre>
 */
@Entity(
        tableName = "sync_queue",
        indices = {
                @Index(value = {"status"}),
                @Index(value = {"entity_type", "entity_id"})
        }
)
public class SyncQueueEntity {

    // -------------------------------------------------------------------------
    // Entity type constants
    // -------------------------------------------------------------------------

    /** Indicates that the queued item is a {@link TransactionEntity}. */
    public static final String ENTITY_TYPE_TRANSACTION = "TRANSACTION";

    // -------------------------------------------------------------------------
    // Action constants
    // -------------------------------------------------------------------------

    /** The queued operation is a creation of a new remote resource. */
    public static final String ACTION_CREATE = "CREATE";

    /** The queued operation is an update of an existing remote resource. */
    public static final String ACTION_UPDATE = "UPDATE";

    /** The queued operation is a deletion of a remote resource. */
    public static final String ACTION_DELETE = "DELETE";

    // -------------------------------------------------------------------------
    // Status constants
    // -------------------------------------------------------------------------

    /** The item is waiting to be picked up by the sync worker. */
    public static final String STATUS_PENDING = "PENDING";

    /**
     * The sync worker has picked up the item and is currently processing it.
     * If the app restarts while an item is in this state it should be reset
     * to {@link #STATUS_PENDING}.
     */
    public static final String STATUS_PROCESSING = "PROCESSING";

    /**
     * The item was successfully synced to the remote server.
     * Items in this state are eligible for periodic cleanup.
     */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * The item failed to sync and has exhausted its retry budget.
     * Manual intervention or a compensating action is required.
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
     * The type of entity being synced.
     * Currently always {@link #ENTITY_TYPE_TRANSACTION}.
     */
    @NonNull
    @ColumnInfo(name = "entity_type")
    private String entityType;

    /** The local database ID of the entity being synced. */
    @ColumnInfo(name = "entity_id")
    private long entityId;

    /**
     * The remote API action to perform.
     * One of {@link #ACTION_CREATE}, {@link #ACTION_UPDATE}, or {@link #ACTION_DELETE}.
     */
    @NonNull
    @ColumnInfo(name = "action")
    private String action;

    /**
     * JSON-serialized payload to send to the remote API.
     * The exact schema depends on {@link #entityType} and {@link #action}.
     */
    @NonNull
    @ColumnInfo(name = "payload")
    private String payload;

    /**
     * Current processing status of this sync item.
     * One of {@link #STATUS_PENDING}, {@link #STATUS_PROCESSING},
     * {@link #STATUS_SUCCESS}, or {@link #STATUS_FAILED}.
     */
    @NonNull
    @ColumnInfo(name = "status")
    private String status;

    /**
     * Number of times the sync worker has attempted to process this item.
     * Incremented on every failed attempt.
     */
    @ColumnInfo(name = "retry_count")
    private int retryCount;

    /**
     * The error message from the most recent failed sync attempt.
     * {@code null} if no attempt has been made yet or the last attempt succeeded.
     */
    @Nullable
    @ColumnInfo(name = "last_error")
    private String lastError;

    /** Unix timestamp (milliseconds) when this queue entry was created. */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    /** Unix timestamp (milliseconds) when this queue entry was last updated. */
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default no-arg constructor required by Room.
     */
    public SyncQueueEntity() {
        long now = System.currentTimeMillis();
        this.status = STATUS_PENDING;
        this.retryCount = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Convenience constructor for enqueuing a new sync item.
     *
     * @param entityType one of the {@code ENTITY_TYPE_*} constants
     * @param entityId   local DB primary key of the entity to sync
     * @param action     one of the {@code ACTION_*} constants
     * @param payload    JSON string body to send to the remote API
     */
    @Ignore
    public SyncQueueEntity(
            @NonNull String entityType,
            long entityId,
            @NonNull String action,
            @NonNull String payload) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.payload = payload;
        long now = System.currentTimeMillis();
        this.status = STATUS_PENDING;
        this.retryCount = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getEntityType() { return entityType; }
    public void setEntityType(@NonNull String entityType) { this.entityType = entityType; }

    public long getEntityId() { return entityId; }
    public void setEntityId(long entityId) { this.entityId = entityId; }

    @NonNull
    public String getAction() { return action; }
    public void setAction(@NonNull String action) { this.action = action; }

    @NonNull
    public String getPayload() { return payload; }
    public void setPayload(@NonNull String payload) { this.payload = payload; }

    @NonNull
    public String getStatus() { return status; }
    public void setStatus(@NonNull String status) { this.status = status; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    @Nullable
    public String getLastError() { return lastError; }
    public void setLastError(@Nullable String lastError) { this.lastError = lastError; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "SyncQueueEntity{" +
                "id=" + id +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", action='" + action + '\'' +
                ", status='" + status + '\'' +
                ", retryCount=" + retryCount +
                ", lastError='" + lastError + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
