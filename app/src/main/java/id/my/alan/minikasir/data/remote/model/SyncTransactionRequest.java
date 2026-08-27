package id.my.alan.minikasir.data.remote.model;

import java.util.List;

/**
 * Request body sent to {@code POST /api/transactions/sync}.
 *
 * <p>Contains the full transaction header plus a list of line items so the
 * server can recreate the sale record and its constituent products in one call.
 */
public class SyncTransactionRequest {

    /** Unique, human-readable transaction code (e.g. {@code TRX-20260824-001}). */
    private String transactionCode;

    /** Grand total of the transaction in the smallest currency unit (e.g. cents / sen). */
    private long totalAmount;

    /** Number of distinct product lines in this transaction. */
    private int itemCount;

    /** Unix-epoch milliseconds when the transaction was created on the device. */
    private long createdAt;

    /** Line items that make up this transaction. */
    private List<SyncItemDto> items;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public SyncTransactionRequest() {
    }

    public SyncTransactionRequest(
            String transactionCode,
            long totalAmount,
            int itemCount,
            long createdAt,
            List<SyncItemDto> items) {
        this.transactionCode = transactionCode;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.createdAt = createdAt;
        this.items = items;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<SyncItemDto> getItems() {
        return items;
    }

    public void setItems(List<SyncItemDto> items) {
        this.items = items;
    }

    // =========================================================================
    // Inner DTO
    // =========================================================================

    /**
     * A single line item within a sync request.
     */
    public static class SyncItemDto {

        /** Primary key of the product in the local Room database and remote catalogue. */
        private long productId;

        /** Snapshot of the product name at the time of sale. */
        private String productName;

        /** Unit price in the smallest currency unit at the time of sale. */
        private long price;

        /** Number of units sold. */
        private int quantity;

        /** {@code price * quantity}, pre-computed on the device. */
        private long subtotal;

        public SyncItemDto() {
        }

        public SyncItemDto(long productId, String productName, long price, int quantity, long subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }

        public long getProductId() {
            return productId;
        }

        public void setProductId(long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public long getPrice() {
            return price;
        }

        public void setPrice(long price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public long getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(long subtotal) {
            this.subtotal = subtotal;
        }

        @Override
        public String toString() {
            return "SyncItemDto{"
                    + "productId=" + productId
                    + ", productName='" + productName + '\''
                    + ", price=" + price
                    + ", quantity=" + quantity
                    + ", subtotal=" + subtotal
                    + '}';
        }
    }

    @Override
    public String toString() {
        return "SyncTransactionRequest{"
                + "transactionCode='" + transactionCode + '\''
                + ", totalAmount=" + totalAmount
                + ", itemCount=" + itemCount
                + ", createdAt=" + createdAt
                + ", items=" + items
                + '}';
    }
}
