package id.my.alan.minikasir.data.repository;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.my.alan.minikasir.data.local.dao.SyncQueueDao;
import id.my.alan.minikasir.data.local.dao.TransactionDao;
import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.SyncQueueEntity;
import id.my.alan.minikasir.data.local.entity.TransactionEntity;
import id.my.alan.minikasir.data.local.entity.TransactionItemEntity;

/**
 * Repository that manages transaction data, including local persistence and
 * enqueueing records for remote synchronisation via the sync queue.
 *
 * <p>Write paths run on a dedicated single-thread executor to ensure serial,
 * off-main-thread execution. Reactive reads return {@link LiveData} whose
 * updates Room delivers automatically.
 */
public class TransactionRepository {

    private static final String TAG = "TransactionRepository";

    /** Sync status constants mirroring the values used in {@link SyncQueueEntity}. */
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SYNCED  = "SYNCED";
    public static final String STATUS_FAILED  = "FAILED";

    private final TransactionDao transactionDao;
    private final SyncQueueDao   syncQueueDao;
    private final Gson           gson;

    /**
     * Single-thread executor guarantees that concurrent writes are serialized
     * and that database operations never occur on the main thread.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Constructs the repository from the application-level {@link AppDatabase}.
     *
     * @param db the Room database instance
     */
    public TransactionRepository(AppDatabase db) {
        this.transactionDao = db.transactionDao();
        this.syncQueueDao   = db.syncQueueDao();
        this.gson           = new Gson();
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Persists a complete transaction – header row plus all line items – in a
     * single atomic operation, then adds a {@link SyncQueueEntity} entry so the
     * background sync worker can push it to the server.
     *
     * <p>All work is dispatched to the background executor; this method returns
     * immediately on the calling thread.
     *
     * @param transaction the transaction header entity
     * @param items       the line items belonging to this transaction
     */
    public void createTransaction(TransactionEntity transaction, List<TransactionItemEntity> items) {
        executor.execute(() -> {
            // 1. Persist transaction header and get the generated primary key.
            long transactionId = transactionDao.insertTransaction(transaction);

            // 2. Bind the generated ID to every line item and persist them all.
            for (TransactionItemEntity item : items) {
                item.setTransactionId(transactionId);
            }
            transactionDao.insertItems(items);

            // 3. Build a JSON payload for the sync queue so the worker can
            //    reconstruct the full request body without extra DB queries.
            String payload = gson.toJson(transaction);

            SyncQueueEntity queueEntry = new SyncQueueEntity();
            queueEntry.setTransactionId(transactionId);
            queueEntry.setTransactionCode(transaction.getTransactionCode());
            queueEntry.setPayload(payload);
            queueEntry.setStatus(STATUS_PENDING);
            queueEntry.setRetryCount(0);
            queueEntry.setCreatedAt(System.currentTimeMillis());

            syncQueueDao.insert(queueEntry);
        });
    }

    /**
     * Updates the sync status and server-acknowledged timestamp of a transaction.
     *
     * @param id       the transaction's primary key
     * @param status   new status string (e.g. {@link #STATUS_SYNCED})
     * @param syncedAt Unix-epoch milliseconds of the server sync acknowledgement
     */
    public void updateTransactionStatus(long id, String status, long syncedAt) {
        executor.execute(() -> transactionDao.updateStatus(id, status, syncedAt));
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link LiveData} stream of all transactions ordered by creation
     * date descending (newest first). Room notifies observers on every change.
     *
     * @return observable list of all {@link TransactionEntity} records
     */
    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    /**
     * Performs a <em>synchronous</em> (blocking) query for transactions matching
     * the given status. Must be called from a background thread.
     *
     * @param status the status string to filter on (e.g. {@code "PENDING"})
     * @return list of matching {@link TransactionEntity} records
     */
    public List<TransactionEntity> getTransactionsByStatus(String status) {
        return transactionDao.getTransactionsByStatus(status);
    }

    /**
     * Returns a {@link LiveData} count of transactions whose sync status is
     * {@link #STATUS_PENDING}. Useful for showing a badge in the UI.
     *
     * @return observable integer count of pending transactions
     */
    public LiveData<Integer> getPendingCount() {
        return syncQueueDao.getPendingCount();
    }
}
