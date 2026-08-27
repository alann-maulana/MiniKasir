package id.my.alan.minikasir.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import androidx.room.Ignore;

/**
 * Room entity representing a product in the MiniKasir POS system.
 * Mapped to the "products" table in the local SQLite database.
 */
@Entity(tableName = "products")
public class ProductEntity {

    /** Auto-generated primary key. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    /** Display name of the product. Cannot be null. */
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    /** Price of the product in Indonesian Rupiah (IDR). */
    @ColumnInfo(name = "price")
    private long price;

    /** Current stock quantity of the product. */
    @ColumnInfo(name = "stock")
    private int stock;

    /** Optional description or notes about the product. */
    @ColumnInfo(name = "description")
    private String description;

    /** Unix timestamp (milliseconds) when this record was created. */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    /** Unix timestamp (milliseconds) when this record was last updated. */
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Default no-arg constructor required by Room.
     */
    public ProductEntity() {
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Convenience constructor for creating a new product.
     *
     * @param name        product display name
     * @param price       price in Rupiah
     * @param stock       initial stock quantity
     * @param description optional product description
     */
    @Ignore
    public ProductEntity(@NonNull String name, long price, int stock, String description) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
