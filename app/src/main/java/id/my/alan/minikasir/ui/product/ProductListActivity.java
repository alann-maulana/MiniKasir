package id.my.alan.minikasir.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.data.local.entity.ProductEntity;
import id.my.alan.minikasir.databinding.ActivityProductListBinding;
import id.my.alan.minikasir.ui.sync.SyncStatusActivity;
import id.my.alan.minikasir.ui.transaction.TransactionActivity;
import id.my.alan.minikasir.ui.transaction.TransactionHistoryActivity;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private ActivityProductListBinding binding;
    private ProductViewModel viewModel;
    private ProductAdapter adapter;
    private int pendingSyncCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(this);
        binding.recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProducts.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getProducts().observe(this, products -> {
            adapter.submitList(products);
            if (products == null || products.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.recyclerProducts.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                binding.recyclerProducts.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getPendingSyncCount().observe(this, count -> {
            pendingSyncCount = count != null ? count : 0;
            invalidateOptionsMenu();
        });
    }

    private void setupListeners() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            startActivity(intent);
        });

        binding.fabCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_list, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setQueryHint(getString(R.string.search_product_hint));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        viewModel.setSearchQuery(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        viewModel.setSearchQuery(newText);
                        return true;
                    }
                });
            }
        }

        MenuItem syncItem = menu.findItem(R.id.action_sync_status);
        if (syncItem != null && pendingSyncCount > 0) {
            syncItem.setTitle(getString(R.string.menu_sync_status) + " (" + pendingSyncCount + ")");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new_transaction) {
            startActivity(new Intent(this, TransactionActivity.class));
            return true;
        } else if (id == R.id.action_history) {
            startActivity(new Intent(this, TransactionHistoryActivity.class));
            return true;
        } else if (id == R.id.action_sync_status) {
            startActivity(new Intent(this, SyncStatusActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProductEdit(ProductEntity product) {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.getId());
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT_NAME, product.getName());
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT_PRICE, product.getPrice());
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT_STOCK, product.getStock());
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT_DESC, product.getDescription());
        startActivity(intent);
    }

    @Override
    public void onProductAddToCart(ProductEntity product) {
        // Direct transition to cart with product pre-selected
        Intent intent = new Intent(this, TransactionActivity.class);
        intent.putExtra(TransactionActivity.EXTRA_PREFILL_PRODUCT_ID, product.getId());
        startActivity(intent);
    }
}
