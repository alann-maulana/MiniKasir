package id.my.alan.minikasir.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a single line item within a transaction.
 * Mapped to the "transaction_items" table in the local SQLite database.
 *
 * <p>Each {@link TransactionItemEntity} belongs to exactly one
 * {@link TransactionEntity} and references exactly one {@link ProductEntity}.
 * The product name and price are denormalized here so that historical
 * transaction records remain accurate even if the product catalogue changes.
 */
@Entity(
        tableName = "transaction_items",
        indices = {
                @Index(value = {"transaction_id"}),
                @Index(value = {"product_id"})
        },
        foreignKeys = {
                @ForeignKey(
                        entity = TransactionEntity.class,
                        parentColumns = "id",
                        childColumns = "transaction_id",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ProductEntity.class,
                        parentColumns = "id",
                        childColumns = "product_id",
                        onDelete = ForeignKey.RESTRICT,
                        onUpdate = ForeignKey.CASCADE
                )
        }
)
public class TransactionItemEntity {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /** Auto-generated primary key. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    /**
     * Foreign key referencing {@link TransactionEntity#getId()}.
     * Deletion of the parent transaction cascades to this item.
     */
    @ColumnInfo(name = "transaction_id")
    private long transactionId;

    /**
     * Foreign key referencing {@link ProductEntity#getId()}.
     * Deletion of a product is restricted while transaction items exist.
     */
    @ColumnInfo(name = "product_id")
    private long productId;

    /**
     * Snapshot of the product name at the time of sale.
     * Preserved independently of future product edits.
     */
    @NonNull
    @ColumnInfo(name = "product_name")
    private String productName;

    /**
     * Snapshot of the unit price (in Rupiah) at the time of sale.
     * Preserved independently of future product price changes.
     */
    @ColumnInfo(name = "price")
    private long price;

    /** Number of units sold in this line item. */
    @ColumnInfo(name = "quantity")
    private int quantity;

    /**
     * Pre-computed subtotal: {@code price * quantity}.
     * Stored explicitly to avoid repeated computation and rounding issues.
     */
    @ColumnInfo(name = "subtotal")
    private long subtotal;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default no-arg constructor required by Room.
     */
    public TransactionItemEntity() {
    }

    /**
     * Convenience constructor for creating a transaction line item.
     *
     * @param transactionId ID of the parent {@link TransactionEntity}
     * @param productId     ID of the {@link ProductEntity} being sold
     * @param productName   snapshot of the product name
     * @param price         unit price in Rupiah at time of sale
     * @param quantity      units sold
     */
    @Ignore
    public TransactionItemEntity(
            long transactionId,
            long productId,
            @NonNull String productName,
            long price,
            int quantity) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTransactionId() { return transactionId; }
    public void setTransactionId(long transactionId) { this.transactionId = transactionId; }

    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }

    @NonNull
    public String getProductName() { return productName; }
    public void setProductName(@NonNull String productName) { this.productName = productName; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getSubtotal() { return subtotal; }
    public void setSubtotal(long subtotal) { this.subtotal = subtotal; }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TransactionItemEntity{" +
                "id=" + id +
                ", transactionId=" + transactionId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}
