package id.my.alan.minikasir.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import id.my.alan.minikasir.data.local.entity.SyncQueueEntity;

/**
 * Room Data Access Object (DAO) for {@link SyncQueueEntity}.
 *
 * <p>The sync queue implements the <em>outbox pattern</em>: local writes are
 * committed immediately and a corresponding queue entry is created. A
 * WorkManager job periodically drains the queue by forwarding payloads to the
 * remote API and updating the item's status accordingly.
 */
@Dao
public interface SyncQueueDao {

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    /**
     * Inserts a new item into the sync queue.
     *
     * @param syncQueueEntity the queue entry to insert
     * @return the newly generated row ID (primary key)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSyncItem(SyncQueueEntity syncQueueEntity);

    /**
     * Updates the processing status of a queue item, optionally recording
     * an error message and the timestamp of the update.
     *
     * @param id        primary key of the {@link SyncQueueEntity}
     * @param status    new status value (one of the {@code STATUS_*} constants)
     * @param error     error message to record, or {@code null} if none
     * @param updatedAt current timestamp in milliseconds
     */
    @Query("UPDATE sync_queue SET status = :status, last_error = :error, " +
           "updated_at = :updatedAt WHERE id = :id")
    void updateStatus(long id, String status, String error, long updatedAt);

    /**
     * Atomically increments the retry counter for a queue item and updates
     * the {@code updated_at} timestamp.
     *
     * @param id        primary key of the {@link SyncQueueEntity}
     * @param updatedAt current timestamp in milliseconds
     */
    @Query("UPDATE sync_queue SET retry_count = retry_count + 1, " +
           "updated_at = :updatedAt WHERE id = :id")
    void incrementRetry(long id, long updatedAt);

    /**
     * Permanently removes all queue entries that have been successfully synced.
     * Should be called periodically (e.g. once a day) to keep the table lean.
     */
    @Query("DELETE FROM sync_queue WHERE status = 'SUCCESS'")
    void deleteSuccessfulItems();

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Returns all queue items whose status is {@code PENDING}, ordered by
     * creation time so that older items are processed first (FIFO).
     *
     * @return list of pending {@link SyncQueueEntity} objects
     */
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at ASC")
    List<SyncQueueEntity> getPendingItems();

    /**
     * Returns all queue items matching the given status value.
     *
     * @param status one of the {@code STATUS_*} constants defined on {@link SyncQueueEntity}
     * @return list of matching {@link SyncQueueEntity} objects
     */
    @Query("SELECT * FROM sync_queue WHERE status = :status ORDER BY created_at ASC")
    List<SyncQueueEntity> getItemsByStatus(String status);

    /**
     * Returns a reactive count of items currently in {@code PENDING} status.
     * Useful for displaying a badge or sync indicator in the UI.
     *
     * @return {@link LiveData} emitting the pending item count
     */
    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    LiveData<Integer> getPendingCount();

    /**
     * Returns a reactive list of all sync queue items ordered newest first.
     */
    @Query("SELECT * FROM sync_queue ORDER BY created_at DESC")
    LiveData<List<SyncQueueEntity>> getAllQueueItems();
}
