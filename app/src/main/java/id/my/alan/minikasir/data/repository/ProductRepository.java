package id.my.alan.minikasir.data.repository;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.my.alan.minikasir.data.local.dao.ProductDao;
import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.ProductEntity;

/**
 * Repository that mediates access to product data stored in the local Room database.
 *
 * <p>All write operations and non-reactive reads are executed on a background
 * {@link ExecutorService} to avoid blocking the main thread. Reactive reads
 * return {@link LiveData} which Room delivers on the main thread automatically.
 *
 * <p>Typical usage in a ViewModel:
 * <pre>
 *     ProductRepository repo = new ProductRepository(AppDatabase.getInstance(context));
 *     LiveData<List<ProductEntity>> products = repo.getAllProducts();
 * </pre>
 */
public class ProductRepository {

    private static final String TAG = "ProductRepository";

    private final ProductDao productDao;

    /**
     * Single-thread executor used for all database write operations so that
     * they are serialized and never executed on the main thread.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Constructs the repository from the application-level {@link AppDatabase}.
     *
     * @param db the Room database instance
     */
    public ProductRepository(AppDatabase db) {
        this.productDao = db.productDao();
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    /**
     * Returns a {@link LiveData} stream of all products ordered by name.
     * Room automatically updates the stream whenever the underlying table changes.
     *
     * @return observable list of all {@link ProductEntity} records
     */
    public LiveData<List<ProductEntity>> getAllProducts() {
        return productDao.getAllProducts();
    }

    /**
     * Returns a {@link LiveData} stream for a single product identified by its
     * primary key. The stream emits {@code null} if the product does not exist
     * or has been deleted.
     *
     * @param id the product primary key
     * @return observable {@link ProductEntity}, or {@code null} if not found
     */
    public LiveData<ProductEntity> getProductById(long id) {
        return productDao.getProductById(id);
    }

    /**
     * Performs a case-insensitive keyword search across product names and
     * returns a reactive stream of matching results.
     *
     * @param query the search keyword (may be partial)
     * @return observable list of matching {@link ProductEntity} records
     */
    public LiveData<List<ProductEntity>> searchProducts(String query) {
        return productDao.searchProducts("%" + query + "%");
    }

    // -------------------------------------------------------------------------
    // Write operations (background-threaded)
    // -------------------------------------------------------------------------

    /**
     * Inserts a new product into the database.
     * If a product with the same primary key already exists it will be replaced.
     *
     * @param product the product to insert
     */
    public void insertProduct(ProductEntity product) {
        executor.execute(() -> productDao.insert(product));
    }

    /**
     * Updates an existing product record. The product is matched by its primary key.
     *
     * @param product the product with updated field values
     */
    public void updateProduct(ProductEntity product) {
        executor.execute(() -> productDao.update(product));
    }

    /**
     * Deletes a product from the database. The product is matched by its primary key.
     *
     * @param product the product to delete
     */
    public void deleteProduct(ProductEntity product) {
        executor.execute(() -> productDao.delete(product));
    }
}
