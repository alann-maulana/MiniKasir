package id.my.alan.minikasir.ui.transaction;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.data.local.entity.ProductEntity;
import id.my.alan.minikasir.databinding.ActivityTransactionBinding;
import id.my.alan.minikasir.util.CurrencyUtils;

public class TransactionActivity extends AppCompatActivity 
        implements CartAdapter.OnCartItemActionListener, 
                   TransactionProductAdapter.OnProductSelectListener {

    public static final String EXTRA_PREFILL_PRODUCT_ID = "extra_prefill_product_id";

    private ActivityTransactionBinding binding;
    private TransactionViewModel viewModel;
    private CartAdapter cartAdapter;
    private TransactionProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupCartRecycler();
        setupProductPickRecycler();
        setupObservers();
        setupListeners();
        handlePrefillIntent();
    }

    private void setupCartRecycler() {
        cartAdapter = new CartAdapter(this);
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerCart.setAdapter(cartAdapter);
    }

    private void setupProductPickRecycler() {
        productAdapter = new TransactionProductAdapter(this);
        binding.recyclerProductPick.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerProductPick.setAdapter(productAdapter);
    }

    private void setupObservers() {
        viewModel.getCartItems().observe(this, items -> {
            cartAdapter.submitList(items);
            boolean isEmpty = (items == null || items.isEmpty());
            binding.tvEmptyCart.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.btnCheckout.setEnabled(!isEmpty);
        });

        viewModel.getCartTotal().observe(this, total -> {
            binding.tvTotalAmount.setText(CurrencyUtils.formatRupiah(total != null ? total : 0L));
        });

        viewModel.getAllProducts().observe(this, products -> {
            productAdapter.submitList(products);
        });

        viewModel.getCheckoutSuccessEvent().observe(this, code -> {
            if (code != null) {
                Snackbar.make(binding.getRoot(), 
                        "Transaksi tersimpan! (" + code + ")", 
                        Snackbar.LENGTH_LONG)
                        .setAction("Tutup", v -> finish())
                        .show();
            }
        });

        viewModel.getCheckoutErrorEvent().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnClearCart.setOnClickListener(v -> viewModel.clearCart());

        binding.btnCheckout.setOnClickListener(v -> {
            EditText etNote = new EditText(this);
            etNote.setHint(R.string.hint_transaction_note);

            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.confirm_checkout_title)
                    .setMessage(R.string.confirm_checkout_message)
                    .setView(etNote)
                    .setPositiveButton(R.string.btn_pay, (dialog, which) -> {
                        String note = etNote.getText().toString();
                        viewModel.checkout(note);
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        });
    }

    private void handlePrefillIntent() {
        long prefillId = getIntent().getLongExtra(EXTRA_PREFILL_PRODUCT_ID, -1);
        if (prefillId > 0) {
            viewModel.getAllProducts().observe(this, products -> {
                if (products != null) {
                    for (ProductEntity p : products) {
                        if (p.getId() == prefillId) {
                            viewModel.addToCart(p);
                            break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onIncrement(long productId) {
        viewModel.incrementCartItem(productId);
    }

    @Override
    public void onDecrement(long productId) {
        viewModel.decrementCartItem(productId);
    }

    @Override
    public void onRemove(long productId) {
        viewModel.removeFromCart(productId);
    }

    @Override
    public void onProductSelected(ProductEntity product) {
        viewModel.addToCart(product);
    }
}
