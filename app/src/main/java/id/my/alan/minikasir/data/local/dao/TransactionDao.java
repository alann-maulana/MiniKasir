package id.my.alan.minikasir.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Transaction;

import java.util.List;

import id.my.alan.minikasir.data.local.entity.TransactionEntity;
import id.my.alan.minikasir.data.local.entity.TransactionItemEntity;

/**
 * Room Data Access Object (DAO) for {@link TransactionEntity} and
 * {@link TransactionItemEntity}.
 *
 * <p>Transactions and their line items are always written atomically using the
 * {@link #insertFullTransaction(TransactionEntity, List)} helper (or manually
 * inside a {@link androidx.room.RoomDatabase} transaction block).
 */
@Dao
public abstract class TransactionDao {

    // =========================================================================
    // Inner POJO: TransactionWithItems
    // =========================================================================

    /**
     * Composite POJO that carries a {@link TransactionEntity} together with all
     * of its associated {@link TransactionItemEntity} line items.
     *
     * <p>Room populates this object automatically when returned from a
     * {@link Transaction}-annotated query method.
     */
    public static class TransactionWithItems {

        /**
         * The parent transaction header record.
         */
        @Embedded
        public TransactionEntity transaction;

        /**
         * All line items that belong to {@link #transaction}.
         * Populated by Room via the {@code transaction_id} foreign key.
         */
        @Relation(
                parentColumn = "id",
                entityColumn = "transaction_id"
        )
        public List<TransactionItemEntity> items;

        /**
         * Computes the total number of individual units across all line items.
         *
         * @return sum of {@link TransactionItemEntity#getQuantity()} for each item
         */
        public int getTotalQuantity() {
            if (items == null) return 0;
            int total = 0;
            for (TransactionItemEntity item : items) {
                total += item.getQuantity();
            }
            return total;
        }
    }

    // =========================================================================
    // Write operations
    // =========================================================================

    /**
     * Inserts a single transaction header row.
     * Must be called within a database transaction when also inserting items.
     *
     * @param transaction the transaction to insert
     * @return the newly generated row ID (primary key)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertTransaction(TransactionEntity transaction);

    /**
     * Bulk-inserts a list of transaction line items in a single statement.
     *
     * @param items the line items to insert; must all reference a valid
     *              {@code transaction_id} that already exists in the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertTransactionItems(List<TransactionItemEntity> items);

    /**
     * Atomically inserts a full transaction (header + all line items) inside
     * a single Room database transaction, guaranteeing that either both writes
     * succeed or neither is committed.
     *
     * <p>Call this method from a background thread.
     *
     * @param transaction the transaction header
     * @param items       the associated line items
     * @return the primary key of the newly inserted transaction
     */
    @Transaction
    public long insertFullTransaction(TransactionEntity transaction, List<TransactionItemEntity> items) {
        long transactionId = insertTransaction(transaction);
        if (items != null && !items.isEmpty()) {
            for (TransactionItemEntity item : items) {
                item.setTransactionId(transactionId);
            }
            insertTransactionItems(items);
        }
        return transactionId;
    }

    /**
     * Updates the sync status of a transaction, recording the timestamp at
     * which the sync occurred.
     *
     * @param id        primary key of the {@link TransactionEntity}
     * @param status    new status (one of {@link TransactionEntity#STATUS_PENDING},
     *                  {@link TransactionEntity#STATUS_SYNCED},
     *                  or {@link TransactionEntity#STATUS_FAILED})
     * @param syncedAt  Unix timestamp (ms) of the sync attempt; pass {@code 0}
     *                  if not applicable (e.g. when marking as FAILED)
     */
    @Query("UPDATE transactions SET status = :status, synced_at = :syncedAt WHERE id = :id")
    public abstract void updateTransactionStatus(long id, String status, long syncedAt);

    // =========================================================================
    // Read operations
    // =========================================================================

    /**
     * Returns a reactive stream of all transactions ordered by creation date
     * descending (newest first). Room automatically re-emits the list whenever
     * the underlying table changes.
     *
     * @return {@link LiveData} wrapping the full transaction list
     */
    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    public abstract LiveData<List<TransactionEntity>> getAllTransactions();

    /**
     * Returns a plain (non-reactive) snapshot of all transactions that match
     * the given status. Useful for the sync worker to find pending items.
     *
     * @param status the status to filter by (e.g. {@link TransactionEntity#STATUS_PENDING})
     * @return list of matching transactions ordered by creation date ascending
     */
    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY created_at ASC")
    public abstract List<TransactionEntity> getTransactionsByStatus(String status);

    /**
     * Loads a single transaction together with all of its line items.
     * The {@link Transaction} annotation tells Room to run both the parent and
     * child queries inside the same database transaction, preventing
     * inconsistent reads.
     *
     * @param transactionId the primary key of the desired transaction
     * @return a {@link TransactionWithItems} POJO, or {@code null} if not found
     */
    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    public abstract TransactionWithItems getTransactionWithItems(long transactionId);

    /**
     * Returns a reactive count of transactions whose status is
     * {@link TransactionEntity#STATUS_PENDING}.
     * Useful for displaying an unsynchronised-items badge in the UI.
     *
     * @return {@link LiveData} emitting the pending transaction count
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'PENDING'")
    public abstract LiveData<Integer> getPendingTransactionsCount();
}
