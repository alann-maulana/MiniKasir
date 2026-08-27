package id.my.alan.minikasir.ui.sync;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.SyncQueueEntity;

public class SyncStatusViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final LiveData<List<SyncQueueEntity>> queueItems;
    private final LiveData<Integer> pendingCount;

    public SyncStatusViewModel(@NonNull Application application) {
        super(application);
        this.db = AppDatabase.getInstance(application);
        this.queueItems = db.syncQueueDao().getAllQueueItems();
        this.pendingCount = db.syncQueueDao().getPendingCount();
    }

    public LiveData<List<SyncQueueEntity>> getQueueItems() {
        return queueItems;
    }

    public LiveData<Integer> getPendingCount() {
        return pendingCount;
    }
}
