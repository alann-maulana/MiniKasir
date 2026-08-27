package id.my.alan.minikasir;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.worker.SyncWorker;

public class MiniKasirApp extends Application {

    private static final String TAG = "MiniKasirApp";
    private static MiniKasirApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Force light mode regardless of system dark mode setting
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize Room DB early (triggers pre-populate if first launch)
        AppDatabase.getInstance(this);

        // Schedule periodic sync worker
        SyncWorker.schedulePeriodicSync(this);
        Log.d(TAG, "MiniKasir application initialized");
    }

    public static MiniKasirApp getInstance() {
        return instance;
    }
}
