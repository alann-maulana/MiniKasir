package id.my.alan.minikasir.data.remote.model;

/**
 * Generic API response wrapper used for all network responses.
 *
 * <p>All REST endpoints in the MiniKasir backend return a JSON envelope
 * with this shape:
 * <pre>
 * {
 *   "success": true,
 *   "message": "Operation successful",
 *   "data": { ... }
 * }
 * </pre>
 *
 * @param <T> the type of the {@code data} payload
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /** Required no-arg constructor for Gson deserialization. */
    public ApiResponse() {
    }

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // -------------------------------------------------------------------------
    // Static factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates a successful response wrapping the given data payload.
     *
     * @param data the deserialized response body
     * @param <T>  payload type
     * @return     a successful {@link ApiResponse}
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /**
     * Creates an error response with no data payload.
     *
     * @param message human-readable error description
     * @param <T>     payload type (will be {@code null})
     * @return        an error {@link ApiResponse}
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /** Returns {@code true} when the server processed the request without errors. */
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /** Returns a human-readable status message from the server. */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** Returns the deserialized response payload, or {@code null} for error responses. */
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApiResponse{"
                + "success=" + success
                + ", message='" + message + '\''
                + ", data=" + data
                + '}';
    }
}
