package id.my.alan.minikasir.ui.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.data.local.entity.ProductEntity;
import id.my.alan.minikasir.util.CurrencyUtils;

public class TransactionProductAdapter extends RecyclerView.Adapter<TransactionProductAdapter.ViewHolder> {

    public interface OnProductSelectListener {
        void onProductSelected(ProductEntity product);
    }

    private final List<ProductEntity> products = new ArrayList<>();
    private final OnProductSelectListener listener;

    public TransactionProductAdapter(OnProductSelectListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ProductEntity> newProducts) {
        products.clear();
        if (newProducts != null) {
            products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_product_pick, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductEntity item = products.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvPrice;
        private final TextView tvStock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPickName);
            tvPrice = itemView.findViewById(R.id.tvPickPrice);
            tvStock = itemView.findViewById(R.id.tvPickStock);
        }

        public void bind(ProductEntity product, OnProductSelectListener listener) {
            tvName.setText(product.getName());
            tvPrice.setText(CurrencyUtils.formatRupiah(product.getPrice()));
            tvStock.setText("Stok: " + product.getStock());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProductSelected(product);
            });
        }
    }
}
