package id.my.alan.minikasir.data.local.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.ProductEntity;

/**
 * Instrumented integration tests for {@link ProductDao}.
 *
 * <p>Uses an in-memory {@link AppDatabase} so tests are fully self-contained and leave no
 * residual state on the device.  Each test gets a fresh database via {@link #setUp()} and
 * the database is closed in {@link #tearDown()}.
 *
 * <p>{@link InstantTaskExecutorRule} ensures LiveData emissions are dispatched synchronously
 * so we can observe them without additional threading plumbing.
 */
@RunWith(AndroidJUnit4.class)
public class ProductDaoTest {

    /** Ensures Architecture Components run on the test thread. */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase database;
    private ProductDao  productDao;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()   // safe for tests only
                .build();
        productDao = database.productDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ProductEntity makeProduct(String name, long price, int stock) {
        ProductEntity p = new ProductEntity();
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        p.setDescription("Test product: " + name);
        long now = System.currentTimeMillis();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        return p;
    }

    /**
     * Blocks the current thread until the given LiveData emits its first non-null value,
     * with a 2-second timeout.
     *
     * @throws InterruptedException if the wait is interrupted
     * @throws RuntimeException     if the LiveData does not emit within the timeout
     */
    private <T> T getOrAwaitValue(LiveData<T> liveData) throws InterruptedException {
        AtomicReference<T> data = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        androidx.lifecycle.Observer<T> observer = new androidx.lifecycle.Observer<T>() {
            @Override
            public void onChanged(T value) {
                data.set(value);
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        // LiveData must be observed on the main thread; InstantTaskExecutorRule makes this OK.
        liveData.observeForever(observer);

        if (!latch.await(2, TimeUnit.SECONDS)) {
            liveData.removeObserver(observer);
            throw new RuntimeException("LiveData value was never set within the timeout");
        }
        return data.get();
    }

    // =========================================================================
    // insertAndGetProduct
    // =========================================================================

    /**
     * Insert a product and retrieve it by ID; all fields must match the inserted values.
     */
    @Test
    public void insertAndGetProduct_fieldsMatch() {
        ProductEntity product = makeProduct("Nasi Goreng", 15_000L, 50);
        long id = productDao.insertProduct(product);

        ProductEntity retrieved = productDao.getProductById(id);

        assertNotNull("Retrieved product must not be null", retrieved);
        assertEquals("Name must match",  "Nasi Goreng", retrieved.getName());
        assertEquals("Price must match", 15_000L,       retrieved.getPrice());
        assertEquals("Stock must match", 50,            retrieved.getStock());
    }

    // =========================================================================
    // getAllProducts
    // =========================================================================

    /**
     * After inserting 3 products, getAllProducts LiveData must emit a list of size 3.
     */
    @Test
    public void getAllProducts_after3Inserts_sizeEquals3() throws InterruptedException {
        productDao.insertProduct(makeProduct("Kopi",         5_000L, 100));
        productDao.insertProduct(makeProduct("Teh Manis",    3_000L, 80));
        productDao.insertProduct(makeProduct("Jus Alpukat", 12_000L, 30));

        List<ProductEntity> products = getOrAwaitValue(productDao.getAllProducts());

        assertNotNull("Product list must not be null", products);
        assertEquals("Must contain exactly 3 products", 3, products.size());
    }

    /**
     * getAllProducts on an empty database must emit an empty (not null) list.
     */
    @Test
    public void getAllProducts_emptyDatabase_emitsEmptyList() throws InterruptedException {
        List<ProductEntity> products = getOrAwaitValue(productDao.getAllProducts());

        assertNotNull(products);
        assertEquals("Empty database must produce an empty list", 0, products.size());
    }

    // =========================================================================
    // updateProduct
    // =========================================================================

    /**
     * Insert a product, update its name, then retrieve it — the new name must be reflected.
     */
    @Test
    public void updateProduct_nameChanged_retrievedNameIsUpdated() {
        ProductEntity product = makeProduct("Es Teh", 3_000L, 60);
        long id = productDao.insertProduct(product);

        ProductEntity inserted = productDao.getProductById(id);
        assertNotNull(inserted);
        inserted.setName("Es Teh Manis");
        productDao.updateProduct(inserted);

        ProductEntity updated = productDao.getProductById(id);
        assertNotNull("Updated product must not be null", updated);
        assertEquals("Name must reflect the update", "Es Teh Manis", updated.getName());
    }

    /**
     * Updating a product must not change the IDs of other products in the database.
     */
    @Test
    public void updateProduct_doesNotAffectOtherProducts() {
        long id1 = productDao.insertProduct(makeProduct("Produk A", 1_000L, 10));
        long id2 = productDao.insertProduct(makeProduct("Produk B", 2_000L, 20));

        ProductEntity productA = productDao.getProductById(id1);
        assertNotNull(productA);
        productA.setName("Produk A Updated");
        productDao.updateProduct(productA);

        ProductEntity productB = productDao.getProductById(id2);
        assertNotNull(productB);
        assertEquals("Produk B's name must be unchanged", "Produk B", productB.getName());
    }

    // =========================================================================
    // deleteProduct
    // =========================================================================

    /**
     * Insert a product, delete it, then retrieve by ID — the result must be null.
     */
    @Test
    public void deleteProduct_afterDelete_getByIdReturnsNull() {
        ProductEntity product = makeProduct("Pisang Goreng", 2_500L, 25);
        long id = productDao.insertProduct(product);

        ProductEntity inserted = productDao.getProductById(id);
        assertNotNull("Product must exist before deletion", inserted);

        productDao.deleteProduct(inserted);

        ProductEntity afterDelete = productDao.getProductById(id);
        assertNull("Product must be null after deletion", afterDelete);
    }

    /**
     * Deleting one product must not affect the remaining products.
     */
    @Test
    public void deleteProduct_doesNotDeleteOtherProducts() throws InterruptedException {
        long id1 = productDao.insertProduct(makeProduct("Kopi Hitam", 5_000L, 10));
        long id2 = productDao.insertProduct(makeProduct("Kopi Susu",  8_000L, 10));

        ProductEntity product1 = productDao.getProductById(id1);
        assertNotNull(product1);
        productDao.deleteProduct(product1);

        List<ProductEntity> remaining = getOrAwaitValue(productDao.getAllProducts());
        assertNotNull(remaining);
        assertEquals("Only one product must remain", 1, remaining.size());
        assertEquals("Kopi Susu", remaining.get(0).getName());
    }
}
