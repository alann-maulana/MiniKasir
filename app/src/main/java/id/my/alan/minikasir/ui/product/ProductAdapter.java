package id.my.alan.minikasir.ui.product;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.data.local.entity.ProductEntity;
import id.my.alan.minikasir.util.CurrencyUtils;

public class ProductAdapter extends ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductEdit(ProductEntity product);
        void onProductAddToCart(ProductEntity product);
    }

    private final OnProductClickListener listener;

    public ProductAdapter(OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ProductEntity> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<ProductEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProductEntity oldItem, @NonNull ProductEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductEntity oldItem, @NonNull ProductEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    oldItem.getStock() == newItem.getStock();
        }
    };

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductEntity item = getItem(position);
        holder.bind(item, listener);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvPrice;
        private final TextView tvStock;
        private final TextView tvDescription;
        private final ImageButton btnEdit;
        private final MaterialButton btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStock = itemView.findViewById(R.id.tvProductStock);
            tvDescription = itemView.findViewById(R.id.tvProductDescription);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        public void bind(ProductEntity product, OnProductClickListener listener) {
            tvName.setText(product.getName());
            tvPrice.setText(CurrencyUtils.formatRupiah(product.getPrice()));
            tvStock.setText("Stok: " + product.getStock());

            if (product.getDescription() != null && !product.getDescription().trim().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(product.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onProductEdit(product);
            });

            btnAddToCart.setOnClickListener(v -> {
                if (listener != null) listener.onProductAddToCart(product);
            });
        }
    }
}
