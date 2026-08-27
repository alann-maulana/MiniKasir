package id.my.alan.minikasir.util;

public final class Constants {
    private Constants() {}

    public static final String DATABASE_NAME = "minikasir.db";
    public static final String SYNC_WORK_NAME = "minikasir_periodic_sync";
    public static final String SYNC_ONE_TIME_TAG = "minikasir_onetime_sync";
    public static final long SYNC_PERIODIC_INTERVAL_MINUTES = 15; // WorkManager minimum
    public static final int MAX_RETRY_COUNT = 3;
    public static final String DEFAULT_BASE_URL = "http://localhost:8080/";
}
