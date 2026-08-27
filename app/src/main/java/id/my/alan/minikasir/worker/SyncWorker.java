package id.my.alan.minikasir.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.remote.api.ApiClient;
import id.my.alan.minikasir.data.repository.SyncRepository;
import id.my.alan.minikasir.util.Constants;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncWorker started background sync task");
        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            SyncRepository syncRepository = new SyncRepository(db, ApiClient.getApiService());
            SyncRepository.SyncResult result = syncRepository.syncPendingTransactions();

            Log.i(TAG, "SyncWorker finished with result: " + result);
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "SyncWorker encountered unexpected error", e);
            if (getRunAttemptCount() < Constants.MAX_RETRY_COUNT) {
                return Result.retry();
            }
            return Result.failure();
        }
    }

    /**
     * Schedules periodic background sync using WorkManager.
     */
    public static void schedulePeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest periodicRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                Constants.SYNC_PERIODIC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .addTag(Constants.SYNC_WORK_NAME)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
        );
        Log.d(TAG, "Periodic sync worker enqueued (every 15 min with Network constraint)");
    }

    /**
     * Schedules an immediate one-time sync attempt.
     */
    public static void enqueueOneTimeSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .addTag(Constants.SYNC_ONE_TIME_TAG)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                Constants.SYNC_ONE_TIME_TAG,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
        );
        Log.d(TAG, "One-time sync worker enqueued immediately");
    }
}
