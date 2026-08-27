package id.my.alan.minikasir.ui.transaction;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.databinding.ActivityTransactionHistoryBinding;
import id.my.alan.minikasir.worker.SyncWorker;

public class TransactionHistoryActivity extends AppCompatActivity {

    private ActivityTransactionHistoryBinding binding;
    private TransactionViewModel viewModel;
    private TransactionHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new TransactionHistoryAdapter();
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTransactions.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getAllTransactions().observe(this, transactions -> {
            adapter.submitList(transactions);
            boolean isEmpty = (transactions == null || transactions.isEmpty());
            binding.tvEmptyHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerTransactions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.swipeRefresh.setOnRefreshListener(() -> {
            // LiveData auto-refreshes, just trigger sync and stop refresh animation
            SyncWorker.enqueueOneTimeSync(this);
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.fabSync.setOnClickListener(v -> {
            SyncWorker.enqueueOneTimeSync(this);
            Snackbar.make(binding.getRoot(), R.string.snackbar_sync_started, Snackbar.LENGTH_SHORT).show();
        });
    }
}
