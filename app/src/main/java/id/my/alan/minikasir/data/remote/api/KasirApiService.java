package id.my.alan.minikasir.data.remote.api;

import java.util.List;

import id.my.alan.minikasir.data.remote.model.ApiResponse;
import id.my.alan.minikasir.data.remote.model.SyncTransactionRequest;
import id.my.alan.minikasir.data.remote.model.SyncTransactionResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit service interface for the MiniKasir backend API.
 *
 * <p>All methods return {@link Call} objects so callers can choose
 * synchronous ({@code execute()}) or asynchronous ({@code enqueue()}) invocation.
 */
public interface KasirApiService {

    /**
     * Pushes a locally-created transaction to the remote server.
     *
     * <p>Endpoint: {@code POST /api/transactions/sync}
     *
     * @param request the transaction payload including all line items
     * @return a Call resolving to an {@link ApiResponse} containing
     *         {@link SyncTransactionResponse} on success
     */
    @POST("/api/transactions/sync")
    Call<ApiResponse<SyncTransactionResponse>> syncTransaction(
            @Body SyncTransactionRequest request);

    /**
     * Retrieves a list of transactions from the server, optionally filtered by status.
     *
     * <p>Endpoint: {@code GET /api/transactions}
     *
     * @param status optional status filter (e.g. {@code "PENDING"}, {@code "SYNCED"}).
     *               Pass {@code null} to retrieve all transactions.
     * @return a Call resolving to an {@link ApiResponse} containing a list of
     *         {@link SyncTransactionResponse}
     */
    @GET("/api/transactions")
    Call<ApiResponse<List<SyncTransactionResponse>>> getTransactions(
            @Query("status") String status);
}
