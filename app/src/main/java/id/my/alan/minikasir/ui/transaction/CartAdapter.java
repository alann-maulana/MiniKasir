package id.my.alan.minikasir.ui.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.domain.model.CartItem;
import id.my.alan.minikasir.util.CurrencyUtils;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemActionListener {
        void onIncrement(long productId);
        void onDecrement(long productId);
        void onRemove(long productId);
    }

    private final List<CartItem> items = new ArrayList<>();
    private final OnCartItemActionListener listener;

    public CartAdapter(OnCartItemActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvPrice;
        private final TextView tvQuantity;
        private final TextView tvSubtotal;
        private final ImageButton btnMinus;
        private final ImageButton btnPlus;
        private final ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartItemQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvCartItemSubtotal);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnRemove = itemView.findViewById(R.id.btnRemoveCartItem);
        }

        public void bind(CartItem item, OnCartItemActionListener listener) {
            tvName.setText(item.getProductName());
            tvPrice.setText(CurrencyUtils.formatRupiah(item.getUnitPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            tvSubtotal.setText(CurrencyUtils.formatRupiah(item.getSubtotal()));

            btnMinus.setOnClickListener(v -> {
                if (listener != null) listener.onDecrement(item.getProductId());
            });

            btnPlus.setOnClickListener(v -> {
                if (listener != null) listener.onIncrement(item.getProductId());
            });

            btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemove(item.getProductId());
            });
        }
    }
}
