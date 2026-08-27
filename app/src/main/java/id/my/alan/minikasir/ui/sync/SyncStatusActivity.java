package id.my.alan.minikasir.ui.sync;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.data.local.entity.SyncQueueEntity;
import id.my.alan.minikasir.databinding.ActivitySyncStatusBinding;
import id.my.alan.minikasir.util.Constants;
import id.my.alan.minikasir.worker.SyncWorker;

public class SyncStatusActivity extends AppCompatActivity {

    private ActivitySyncStatusBinding binding;
    private SyncStatusViewModel viewModel;
    private SyncQueueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyncStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(SyncStatusViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new SyncQueueAdapter();
        binding.recyclerSyncQueue.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSyncQueue.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getQueueItems().observe(this, items -> {
            adapter.submitList(items);

            int pending = 0;
            int success = 0;
            int failed = 0;

            if (items != null) {
                for (SyncQueueEntity item : items) {
                    if ("SUCCESS".equalsIgnoreCase(item.getStatus()) || "SYNCED".equalsIgnoreCase(item.getStatus())) {
                        success++;
                    } else if ("FAILED".equalsIgnoreCase(item.getStatus())) {
                        failed++;
                    } else {
                        pending++;
                    }
                }
            }

            binding.tvPendingCount.setText(String.valueOf(pending));
            binding.tvSyncedCount.setText(String.valueOf(success));
            binding.tvFailedCount.setText(String.valueOf(failed));

            boolean isEmpty = (items == null || items.isEmpty());
            binding.tvEmptyQueue.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerSyncQueue.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        // Observe WorkManager status
        WorkManager.getInstance(this).getWorkInfosByTagLiveData(Constants.SYNC_ONE_TIME_TAG)
                .observe(this, workInfos -> {
                    if (workInfos != null && !workInfos.isEmpty()) {
                        WorkInfo.State state = workInfos.get(0).getState();
                        if (state == WorkInfo.State.RUNNING) {
                            binding.progressSync.setVisibility(View.VISIBLE);
                            binding.btnSyncNow.setEnabled(false);
                        } else {
                            binding.progressSync.setVisibility(View.GONE);
                            binding.btnSyncNow.setEnabled(true);
                        }
                    }
                });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSyncNow.setOnClickListener(v -> {
            SyncWorker.enqueueOneTimeSync(this);
            Snackbar.make(binding.getRoot(), R.string.snackbar_sync_started, Snackbar.LENGTH_SHORT).show();
        });
    }
}
