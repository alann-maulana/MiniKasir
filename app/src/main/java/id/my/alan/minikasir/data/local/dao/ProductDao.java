package id.my.alan.minikasir.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.my.alan.minikasir.data.local.entity.ProductEntity;

/**
 * Room Data Access Object (DAO) for {@link ProductEntity}.
 *
 * <p>All write operations must be executed on a background thread.
 * Read operations that return {@link LiveData} are automatically observed
 * on the main thread via Room's built-in LiveData integration.
 */
@Dao
public interface ProductDao {

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Returns a reactive stream of all products ordered by name ascending.
     * Room automatically updates the emitted list whenever the underlying table changes.
     *
     * @return {@link LiveData} wrapping the full product list
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    LiveData<List<ProductEntity>> getAllProducts();

    /**
     * Returns a plain (non-reactive) snapshot of all products ordered by name ascending.
     * Useful for one-shot reads on a background thread (e.g. during sync or export).
     *
     * @return list of all {@link ProductEntity} rows
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    List<ProductEntity> getAllProductsList();

    /**
     * Looks up a single product by its primary key.
     *
     * @param id the product's primary key
     * @return the matching {@link ProductEntity}, or {@code null} if not found
     */
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    ProductEntity getProductById(long id);

    /**
     * Full-text search across product names and descriptions.
     * The query is wrapped with {@code %} wildcards so partial matches are returned.
     *
     * @param query search term entered by the user
     * @return reactive list of matching products ordered by name
     */
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' " +
           "OR description LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<ProductEntity>> searchProducts(String query);

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    /**
     * Inserts a new product into the database.
     * Uses {@link OnConflictStrategy#REPLACE} so that upsert semantics can be
     * achieved by the caller when needed.
     *
     * @param product the product to insert
     * @return the newly generated row ID (primary key)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProduct(ProductEntity product);

    /**
     * Updates an existing product record identified by its primary key.
     *
     * @param product the product with updated field values
     */
    @Update
    void updateProduct(ProductEntity product);

    /**
     * Deletes a product from the database.
     * Will fail with a SQLite foreign key constraint if any
     * {@code TransactionItemEntity} still references this product.
     *
     * @param product the product to delete
     */
    @Delete
    void deleteProduct(ProductEntity product);
}
