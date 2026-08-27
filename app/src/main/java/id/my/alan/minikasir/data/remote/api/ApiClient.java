package id.my.alan.minikasir.data.remote.api;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

import id.my.alan.minikasir.BuildConfig;

/**
 * Singleton factory for the Retrofit-based {@link KasirApiService}.
 *
 * <p>Usage:
 * <pre>
 *     KasirApiService service = ApiClient.getService();
 *     service.syncTransaction(request).enqueue(...);
 * </pre>
 *
 * <p>The base URL can be overridden at runtime (useful with MockWebServer in tests):
 * <pre>
 *     ApiClient.setBaseUrl(mockWebServer.url("/").toString());
 *     KasirApiService service = ApiClient.getService();
 * </pre>
 */
public final class ApiClient {

    private static final String TAG = "ApiClient";

    /** Default backend URL. Override via {@link #setBaseUrl(String)} for testing. */
    private static String BASE_URL = "http://localhost:8080/";

    /** Network timeout in seconds applied to connect, read, and write operations. */
    private static final int TIMEOUT_SECONDS = 30;

    /** Cached Retrofit instance. Invalidated when {@link #setBaseUrl(String)} is called. */
    private static Retrofit retrofit;

    /** Cached service proxy. */
    private static KasirApiService apiService;

    // Private constructor – this is a utility/singleton class.
    private ApiClient() {
        throw new UnsupportedOperationException("ApiClient is a singleton utility class");
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the singleton {@link KasirApiService} instance, creating it if necessary.
     *
     * @return the configured Retrofit service proxy
     */
    public static synchronized KasirApiService getService() {
        if (apiService == null) {
            apiService = getRetrofit().create(KasirApiService.class);
        }
        return apiService;
    }

    /**
     * Updates the base URL used for all future API calls.
     *
     * <p><strong>Note:</strong> calling this method invalidates the existing
     * Retrofit and service instances so the next call to {@link #getService()}
     * will create new ones pointed at the new URL.
     *
     * @param newBaseUrl the new base URL (must end with {@code "/"})
     */
    public static synchronized void setBaseUrl(String newBaseUrl) {
        if (newBaseUrl == null || newBaseUrl.isEmpty()) {
            Log.w(TAG, "setBaseUrl called with null/empty URL – ignoring");
            return;
        }
        Log.d(TAG, "Base URL updated: " + newBaseUrl);
        BASE_URL = newBaseUrl;
        retrofit = null;
        apiService = null;
    }

    /**
     * Returns the current base URL.
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds (or returns the cached) {@link Retrofit} instance.
     */
    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(buildOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Builds the {@link OkHttpClient} with timeout configuration and an optional
     * logging interceptor (enabled only in debug builds).
     */
    private static OkHttpClient buildOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                    message -> Log.d(TAG, message));
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();
    }
}
