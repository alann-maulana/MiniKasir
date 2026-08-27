package id.my.alan.minikasir.ui.transaction;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import id.my.alan.minikasir.R;
import id.my.alan.minikasir.data.local.entity.TransactionEntity;
import id.my.alan.minikasir.util.CurrencyUtils;
import id.my.alan.minikasir.util.DateUtils;

public class TransactionHistoryAdapter 
        extends ListAdapter<TransactionEntity, TransactionHistoryAdapter.ViewHolder> {

    public TransactionHistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TransactionEntity> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<TransactionEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
            return oldItem.getTransactionCode().equals(newItem.getTransactionCode()) &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getTotalAmount() == newItem.getTotalAmount();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCode;
        private final TextView tvDate;
        private final TextView tvItemCount;
        private final TextView tvTotal;
        private final TextView tvStatusBadge;
        private final TextView tvNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvTrxCode);
            tvDate = itemView.findViewById(R.id.tvTrxDate);
            tvItemCount = itemView.findViewById(R.id.tvTrxItemCount);
            tvTotal = itemView.findViewById(R.id.tvTrxTotal);
            tvStatusBadge = itemView.findViewById(R.id.tvTrxStatusBadge);
            tvNote = itemView.findViewById(R.id.tvTrxNote);
        }

        public void bind(TransactionEntity item) {
            tvCode.setText(item.getTransactionCode());
            tvDate.setText(DateUtils.formatDateTime(item.getCreatedAt()));
            tvItemCount.setText(item.getItemCount() + " item");
            tvTotal.setText(CurrencyUtils.formatRupiah(item.getTotalAmount()));

            if (item.getNote() != null && !item.getNote().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText("Catatan: " + item.getNote());
            } else {
                tvNote.setVisibility(View.GONE);
            }

            String status = item.getStatus() != null ? item.getStatus() : "PENDING";
            tvStatusBadge.setText(status);

            if ("SYNCED".equalsIgnoreCase(status)) {
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.status_synced)));
            } else if ("FAILED".equalsIgnoreCase(status)) {
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.status_failed)));
            } else {
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.status_pending)));
            }
        }
    }
}
