package id.my.alan.minikasir.data.repository;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import id.my.alan.minikasir.data.local.dao.SyncQueueDao;
import id.my.alan.minikasir.data.local.dao.TransactionDao;
import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.SyncQueueEntity;
import id.my.alan.minikasir.data.remote.api.KasirApiService;
import id.my.alan.minikasir.data.remote.model.ApiResponse;
import id.my.alan.minikasir.data.remote.model.SyncTransactionRequest;
import id.my.alan.minikasir.data.remote.model.SyncTransactionResponse;
import retrofit2.Response;

/**
 * Repository responsible for driving the synchronisation of locally-created
 * transactions to the remote MiniKasir server.
 *
 * <p>Intended to be called from a {@code WorkManager} worker running on a
 * background thread. All Retrofit calls are made <em>synchronously</em> via
 * {@link retrofit2.Call#execute()} so that the caller can block until all
 * pending items have been processed.
 *
 * <p>Retry logic:
 * <ul>
 *   <li>On a transient failure the retry counter is incremented.</li>
 *   <li>After {@link #MAX_RETRY_COUNT} failed attempts the queue entry is
 *       marked {@code FAILED} and will no longer be retried automatically.</li>
 * </ul>
 */
public class SyncRepository {

    private static final String TAG = "SyncRepository";

    /** Maximum number of attempts before a queue entry is permanently marked FAILED. */
    private static final int MAX_RETRY_COUNT = 3;

    // Sync status constants
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUCCESS = "SYNCED";
    private static final String STATUS_FAILED  = "FAILED";

    private final SyncQueueDao    syncQueueDao;
    private final TransactionDao  transactionDao;
    private final KasirApiService apiService;
    private final Gson            gson;

    /**
     * Constructs the repository with all required dependencies.
     *
     * @param db         the Room database instance
     * @param apiService the Retrofit service proxy
     */
    public SyncRepository(AppDatabase db, KasirApiService apiService) {
        this.syncQueueDao   = db.syncQueueDao();
        this.transactionDao = db.transactionDao();
        this.apiService     = apiService;
        this.gson           = new Gson();
    }

    // -------------------------------------------------------------------------
    // Sync logic
    // -------------------------------------------------------------------------

    /**
     * Processes all pending items in the sync queue, calling the remote API for
     * each one and updating the local database with the outcome.
     *
     * <p>This method <strong>blocks the calling thread</strong> until every
     * pending item has been attempted. It should be invoked from a WorkManager
     * {@code Worker.doWork()} method or another managed background context.
     *
     * @return a {@link SyncResult} summarising how many items succeeded, failed,
     *         and were processed in total
     */
    public SyncResult syncPendingTransactions() {
        List<SyncQueueEntity> pendingItems = syncQueueDao.getPendingItems();
        Log.d(TAG, "Starting sync – " + pendingItems.size() + " pending item(s)");

        int successCount = 0;
        int failCount    = 0;

        for (SyncQueueEntity item : pendingItems) {
            boolean success = syncSingleItem(item);
            if (success) {
                successCount++;
            } else {
                failCount++;
            }
        }

        SyncResult result = new SyncResult(successCount, failCount, pendingItems.size());
        Log.d(TAG, "Sync complete: " + result);
        return result;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Attempts to sync a single {@link SyncQueueEntity} to the remote server.
     *
     * @param item the queue entry to process
     * @return {@code true} if the server accepted the transaction; {@code false} otherwise
     */
    private boolean syncSingleItem(SyncQueueEntity item) {
        try {
            SyncTransactionRequest request = buildRequest(item);
            Response<ApiResponse<SyncTransactionResponse>> response =
                    apiService.syncTransaction(request).execute();

            if (response.isSuccessful()
                    && response.body() != null
                    && response.body().isSuccess()) {

                SyncTransactionResponse body = response.body().getData();
                long syncedAt = body != null ? body.getSyncedAt() : System.currentTimeMillis();

                // Mark the queue entry as successfully synced.
                syncQueueDao.updateStatus(item.getId(), STATUS_SUCCESS, null, syncedAt);

                // Update the transaction record to reflect the synced state.
                transactionDao.updateTransactionStatus(item.getEntityId(), TransactionRepository.STATUS_SYNCED, syncedAt);

                Log.d(TAG, "Synced transaction #" + item.getEntityId());
                return true;

            } else {
                String errorMsg = extractErrorMessage(response);
                Log.w(TAG, "Server rejected transaction #" + item.getEntityId() + ": " + errorMsg);
                handleFailure(item, errorMsg);
                return false;
            }

        } catch (IOException e) {
            // Network error (timeout, no connectivity, etc.)
            String errorMsg = "Network error: " + e.getMessage();
            Log.e(TAG, "Failed to sync transaction #" + item.getEntityId(), e);
            handleFailure(item, errorMsg);
            return false;
        }
    }

    /**
     * Deserialises the stored JSON payload back into a {@link SyncTransactionRequest}.
     *
     * @param item the queue entry containing the serialised payload
     * @return a ready-to-send request object
     */
    private SyncTransactionRequest buildRequest(SyncQueueEntity item) {
        return gson.fromJson(item.getPayload(), SyncTransactionRequest.class);
    }

    /**
     * Increments the retry counter and, if the maximum has been reached, marks
     * the queue entry as permanently {@code FAILED}.
     *
     * @param item         the queue entry that failed
     * @param errorMessage a human-readable description of the failure reason
     */
    private void handleFailure(SyncQueueEntity item, String errorMessage) {
        long now = System.currentTimeMillis();
        syncQueueDao.incrementRetry(item.getId(), now);

        int newRetryCount = item.getRetryCount() + 1;
        String newStatus  = (newRetryCount >= MAX_RETRY_COUNT) ? STATUS_FAILED : STATUS_PENDING;

        syncQueueDao.updateStatus(item.getId(), newStatus, errorMessage, now);

        if (STATUS_FAILED.equals(newStatus)) {
            transactionDao.updateTransactionStatus(item.getEntityId(), TransactionRepository.STATUS_FAILED, 0L);
            Log.e(TAG, "Transaction #" + item.getEntityId()
                    + " permanently failed after " + newRetryCount + " attempt(s)");
        }
    }

    /**
     * Attempts to extract a meaningful error message from a non-successful HTTP response.
     *
     * @param response the Retrofit response object
     * @return a short error string
     */
    private String extractErrorMessage(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                return response.errorBody().string();
            } catch (IOException ignored) {
            }
        }
        return "HTTP " + response.code();
    }

    // =========================================================================
    // Result POJO
    // =========================================================================

    /**
     * Immutable summary returned by {@link #syncPendingTransactions()}.
     */
    public static final class SyncResult {

        /** Number of transactions successfully pushed to the server. */
        public final int success;

        /** Number of transactions that failed (including those now permanently failed). */
        public final int failed;

        /** Total number of queue entries that were processed in this run. */
        public final int total;

        public SyncResult(int success, int failed, int total) {
            this.success = success;
            this.failed  = failed;
            this.total   = total;
        }

        @Override
        public String toString() {
            return "SyncResult{success=" + success
                    + ", failed=" + failed
                    + ", total=" + total + '}';
        }
    }
}
