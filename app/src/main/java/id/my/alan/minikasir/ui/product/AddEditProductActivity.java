package id.my.alan.minikasir.ui.product;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.data.local.entity.ProductEntity;
import id.my.alan.minikasir.databinding.ActivityAddEditProductBinding;
import id.my.alan.minikasir.util.CurrencyUtils;

public class AddEditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";
    public static final String EXTRA_PRODUCT_PRICE = "extra_product_price";
    public static final String EXTRA_PRODUCT_STOCK = "extra_product_stock";
    public static final String EXTRA_PRODUCT_DESC = "extra_product_desc";

    private ActivityAddEditProductBinding binding;
    private ProductViewModel viewModel;
    private long editProductId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        checkEditMode();
        setupListeners();
    }

    private void checkEditMode() {
        if (getIntent().hasExtra(EXTRA_PRODUCT_ID)) {
            editProductId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
            String name = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
            long price = getIntent().getLongExtra(EXTRA_PRODUCT_PRICE, 0);
            int stock = getIntent().getIntExtra(EXTRA_PRODUCT_STOCK, 0);
            String desc = getIntent().getStringExtra(EXTRA_PRODUCT_DESC);

            setTitle(R.string.title_edit_product);
            if (binding.etName != null) binding.etName.setText(name);
            if (binding.etPrice != null) binding.etPrice.setText(String.valueOf(price));
            if (binding.etStock != null) binding.etStock.setText(String.valueOf(stock));
            if (binding.etDescription != null) binding.etDescription.setText(desc);

            binding.btnDelete.setVisibility(View.VISIBLE);
        } else {
            setTitle(R.string.title_add_product);
            binding.btnDelete.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> saveProduct());

        binding.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.confirm_delete_title)
                    .setMessage(R.string.confirm_delete_message)
                    .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                        ProductEntity product = new ProductEntity();
                        product.setId(editProductId);
                        viewModel.deleteProduct(product);
                        Toast.makeText(this, R.string.toast_product_deleted, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        });
    }

    private void saveProduct() {
        String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
        String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString().trim() : "";
        String stockStr = binding.etStock.getText() != null ? binding.etStock.getText().toString().trim() : "";
        String desc = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";

        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.error_name_required));
            return;
        } else {
            binding.tilName.setError(null);
        }

        long price = CurrencyUtils.parseRupiah(priceStr);
        if (price <= 0) {
            binding.tilPrice.setError(getString(R.string.error_price_invalid));
            return;
        } else {
            binding.tilPrice.setError(null);
        }

        int stock = 0;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                binding.tilStock.setError(getString(R.string.error_stock_invalid));
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilStock.setError(getString(R.string.error_stock_invalid));
            return;
        }
        binding.tilStock.setError(null);

        ProductEntity product = new ProductEntity();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setDescription(desc);
        long now = System.currentTimeMillis();
        product.setUpdatedAt(now);

        if (editProductId > 0) {
            product.setId(editProductId);
            viewModel.updateProduct(product);
            Toast.makeText(this, R.string.toast_product_updated, Toast.LENGTH_SHORT).show();
        } else {
            product.setCreatedAt(now);
            viewModel.insertProduct(product);
            Toast.makeText(this, R.string.toast_product_saved, Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
