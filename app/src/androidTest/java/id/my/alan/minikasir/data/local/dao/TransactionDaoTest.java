package id.my.alan.minikasir.data.local.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.ProductEntity;
import id.my.alan.minikasir.data.local.entity.TransactionEntity;
import id.my.alan.minikasir.data.local.entity.TransactionItemEntity;

/**
 * Instrumented integration tests for {@link TransactionDao}.
 *
 * <p>Each test runs against a fresh in-memory Room database.  All assertions
 * focus on the DAO contract rather than the underlying SQLite query text.
 */
@RunWith(AndroidJUnit4.class)
public class TransactionDaoTest {

    /** Synchronous LiveData dispatch for test thread. */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase    database;
    private TransactionDao transactionDao;
    private ProductDao     productDao;
    private long           seedProductId1;
    private long           seedProductId2;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        transactionDao = database.transactionDao();
        productDao     = database.productDao();

        // Pre-insert two products so TransactionItem foreign keys are satisfied.
        ProductEntity p1 = new ProductEntity();
        p1.setName("Kopi Hitam"); p1.setPrice(5_000L); p1.setStock(100);
        seedProductId1 = productDao.insertProduct(p1);

        ProductEntity p2 = new ProductEntity();
        p2.setName("Roti Bakar"); p2.setPrice(13_000L); p2.setStock(50);
        seedProductId2 = productDao.insertProduct(p2);
    }

    @After
    public void tearDown() {
        database.close();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private TransactionEntity makeTransaction(String code, String status, long totalAmount) {
        TransactionEntity t = new TransactionEntity();
        t.setTransactionCode(code);
        t.setStatus(status);
        t.setTotalAmount(totalAmount);
        t.setItemCount(1);
        t.setCreatedAt(System.currentTimeMillis());
        t.setSyncedAt(0L);
        return t;
    }

    private TransactionItemEntity makeItem(long transactionId, long productId,
                                           String productName, int qty, long pricePerUnit) {
        TransactionItemEntity item = new TransactionItemEntity();
        item.setTransactionId(transactionId);
        item.setProductId(productId);
        item.setProductName(productName);
        item.setQuantity(qty);
        item.setPrice(pricePerUnit);
        item.setSubtotal((long) qty * pricePerUnit);
        return item;
    }

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

        liveData.observeForever(observer);

        if (!latch.await(2, TimeUnit.SECONDS)) {
            liveData.removeObserver(observer);
            throw new RuntimeException("LiveData value was never set within the timeout");
        }
        return data.get();
    }

    // =========================================================================
    // insertTransaction + insertTransactionItems → getTransactionWithItems
    // =========================================================================

    /**
     * After inserting a transaction and two items, getTransactionWithItems must return the
     * transaction with exactly two associated items.
     */
    @Test
    public void insertTransactionAndItems_getTransactionWithItems_itemsCountMatches() {
        TransactionEntity transaction = makeTransaction("TRX-001", "PENDING", 23_000L);
        long transactionId = transactionDao.insertTransaction(transaction);

        transactionDao.insertTransactionItems(Arrays.asList(
                makeItem(transactionId, seedProductId1, "Kopi Hitam", 2, 5_000L),
                makeItem(transactionId, seedProductId2, "Roti Bakar", 1, 13_000L)
        ));

        TransactionDao.TransactionWithItems result =
                transactionDao.getTransactionWithItems(transactionId);

        assertNotNull("TransactionWithItems must not be null", result);
        assertNotNull("Embedded transaction must not be null", result.transaction);
        assertNotNull("Items list must not be null", result.items);
        assertEquals("Must have exactly 2 items", 2, result.items.size());
    }

    /**
     * A transaction with zero items must return an empty (not null) items list.
     */
    @Test
    public void insertTransactionWithNoItems_getTransactionWithItems_emptyItemsList() {
        TransactionEntity transaction = makeTransaction("TRX-002", "PENDING", 0L);
        long transactionId = transactionDao.insertTransaction(transaction);

        TransactionDao.TransactionWithItems result =
                transactionDao.getTransactionWithItems(transactionId);

        assertNotNull(result);
        assertNotNull(result.items);
        assertEquals("Items list must be empty for a transaction with no items",
                0, result.items.size());
    }

    // =========================================================================
    // getTransactionsByStatus
    // =========================================================================

    /**
     * getTransactionsByStatus("SYNCED") must return only synced transactions.
     */
    @Test
    public void getTransactionsByStatus_returnsOnlyMatchingTransactions() {
        transactionDao.insertTransaction(makeTransaction("TRX-A1", "SYNCED",  10_000L));
        transactionDao.insertTransaction(makeTransaction("TRX-A2", "SYNCED",  20_000L));
        transactionDao.insertTransaction(makeTransaction("TRX-A3", "PENDING", 15_000L));

        List<TransactionEntity> synced = transactionDao.getTransactionsByStatus("SYNCED");

        assertNotNull(synced);
        assertEquals("Must return only 2 SYNCED transactions", 2, synced.size());
        for (TransactionEntity t : synced) {
            assertEquals("Status must be SYNCED", "SYNCED", t.getStatus());
        }
    }

    /**
     * getTransactionsByStatus for a status with no records must return an empty list.
     */
    @Test
    public void getTransactionsByStatus_noMatchingStatus_returnsEmptyList() {
        transactionDao.insertTransaction(makeTransaction("TRX-B1", "SYNCED", 5_000L));

        List<TransactionEntity> pending = transactionDao.getTransactionsByStatus("PENDING");

        assertNotNull(pending);
        assertEquals("Must return empty list when no match", 0, pending.size());
    }

    /**
     * getTransactionsByStatus("PENDING") must not include SYNCED transactions.
     */
    @Test
    public void getTransactionsByStatus_pendingQuery_doesNotReturnSynced() {
        transactionDao.insertTransaction(makeTransaction("TRX-C1", "PENDING", 8_000L));
        transactionDao.insertTransaction(makeTransaction("TRX-C2", "SYNCED", 12_000L));

        List<TransactionEntity> pending = transactionDao.getTransactionsByStatus("PENDING");

        assertNotNull(pending);
        assertEquals(1, pending.size());
        assertEquals("TRX-C1", pending.get(0).getTransactionCode());
    }

    // =========================================================================
    // updateTransactionStatus
    // =========================================================================

    /**
     * updateTransactionStatus must change the status of the specified transaction.
     */
    @Test
    public void updateTransactionStatus_statusIsChanged() {
        TransactionEntity transaction = makeTransaction("TRX-D1", "PENDING", 30_000L);
        long id = transactionDao.insertTransaction(transaction);

        transactionDao.updateTransactionStatus(id, "SYNCED", System.currentTimeMillis());

        List<TransactionEntity> synced = transactionDao.getTransactionsByStatus("SYNCED");

        assertNotNull(synced);
        assertFalse("Synced list must not be empty after status update", synced.isEmpty());

        boolean found = false;
        for (TransactionEntity t : synced) {
            if (t.getTransactionCode().equals("TRX-D1")) {
                found = true;
                assertEquals("Status must be SYNCED", "SYNCED", t.getStatus());
            }
        }
        assertEquals("TRX-D1 must appear in SYNCED list", true, found);
    }

    /**
     * updateTransactionStatus must not change the status of other transactions.
     */
    @Test
    public void updateTransactionStatus_doesNotAffectOtherTransactions() {
        long idToUpdate = transactionDao.insertTransaction(
                makeTransaction("TRX-E1", "PENDING", 5_000L));
        transactionDao.insertTransaction(
                makeTransaction("TRX-E2", "PENDING", 10_000L));

        transactionDao.updateTransactionStatus(idToUpdate, "SYNCED", System.currentTimeMillis());

        List<TransactionEntity> stillPending =
                transactionDao.getTransactionsByStatus("PENDING");

        assertNotNull(stillPending);
        assertEquals("Only TRX-E2 should remain PENDING", 1, stillPending.size());
        assertEquals("TRX-E2", stillPending.get(0).getTransactionCode());
    }
}
