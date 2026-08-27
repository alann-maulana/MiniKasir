package id.my.alan.minikasir.ui.sync;

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
import id.my.alan.minikasir.data.local.entity.SyncQueueEntity;
import id.my.alan.minikasir.util.DateUtils;

public class SyncQueueAdapter extends ListAdapter<SyncQueueEntity, SyncQueueAdapter.ViewHolder> {

    public SyncQueueAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<SyncQueueEntity> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<SyncQueueEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull SyncQueueEntity oldItem, @NonNull SyncQueueEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SyncQueueEntity oldItem, @NonNull SyncQueueEntity newItem) {
            return oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getRetryCount() == newItem.getRetryCount();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sync_queue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEntity;
        private final TextView tvStatusBadge;
        private final TextView tvRetryCount;
        private final TextView tvLastError;
        private final TextView tvCreatedAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEntity = itemView.findViewById(R.id.tvQueueEntity);
            tvStatusBadge = itemView.findViewById(R.id.tvQueueStatusBadge);
            tvRetryCount = itemView.findViewById(R.id.tvQueueRetryCount);
            tvLastError = itemView.findViewById(R.id.tvQueueLastError);
            tvCreatedAt = itemView.findViewById(R.id.tvQueueCreatedAt);
        }

        public void bind(SyncQueueEntity item) {
            tvEntity.setText(item.getEntityType() + " #" + item.getEntityId() + " (" + item.getAction() + ")");
            tvRetryCount.setText("Retry: " + item.getRetryCount());
            tvCreatedAt.setText(DateUtils.formatDateTime(item.getCreatedAt()));

            String status = item.getStatus() != null ? item.getStatus() : "PENDING";
            tvStatusBadge.setText(status);

            if ("SUCCESS".equalsIgnoreCase(status) || "SYNCED".equalsIgnoreCase(status)) {
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.status_synced)));
            } else if ("FAILED".equalsIgnoreCase(status)) {
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.status_failed)));
            } else if ("PROCESSING".equalsIgnoreCase(status)) {
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.status_processing)));
            } else {
                tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), R.color.status_pending)));
            }

            if (item.getLastError() != null && !item.getLastError().isEmpty()) {
                tvLastError.setVisibility(View.VISIBLE);
                tvLastError.setText("Error: " + item.getLastError());
            } else {
                tvLastError.setVisibility(View.GONE);
            }
        }
    }
}
