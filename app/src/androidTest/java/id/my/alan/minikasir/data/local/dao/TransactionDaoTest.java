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

import id.my.alan.minikasir.data.local.AppDatabase;
import id.my.alan.minikasir.data.model.Transaction;
import id.my.alan.minikasir.data.model.TransactionItem;
import id.my.alan.minikasir.data.model.TransactionWithItems;

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
    }

    @After
    public void tearDown() {
        database.close();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Transaction makeTransaction(String code, String status, long totalAmount) {
        Transaction t = new Transaction();
        t.setTransactionCode(code);
        t.setStatus(status);
        t.setTotalAmount(totalAmount);
        t.setCreatedAt(System.currentTimeMillis());
        t.setSynced(false);
        return t;
    }

    private TransactionItem makeItem(long transactionId, String productName,
                                     int qty, long pricePerUnit) {
        TransactionItem item = new TransactionItem();
        item.setTransactionId(transactionId);
        item.setProductName(productName);
        item.setQuantity(qty);
        item.setPricePerUnit(pricePerUnit);
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
    public void insertTransactionAndItems_getTransactionWithItems_itemsCountMatches()
            throws InterruptedException {
        Transaction transaction = makeTransaction("TRX-001", "COMPLETED", 23_000L);
        long transactionId = transactionDao.insertTransaction(transaction);

        transactionDao.insertTransactionItems(Arrays.asList(
                makeItem(transactionId, "Kopi Hitam", 2, 5_000L),
                makeItem(transactionId, "Roti Bakar", 1, 13_000L)
        ));

        TransactionWithItems result = transactionDao.getTransactionWithItems(transactionId);

        assertNotNull("TransactionWithItems must not be null", result);
        assertNotNull("Embedded transaction must not be null", result.getTransaction());
        assertNotNull("Items list must not be null", result.getItems());
        assertEquals("Must have exactly 2 items", 2, result.getItems().size());
    }

    /**
     * A transaction with zero items must return an empty (not null) items list.
     */
    @Test
    public void insertTransactionWithNoItems_getTransactionWithItems_emptyItemsList()
            throws InterruptedException {
        Transaction transaction = makeTransaction("TRX-002", "PENDING", 0L);
        long transactionId = transactionDao.insertTransaction(transaction);

        TransactionWithItems result = transactionDao.getTransactionWithItems(transactionId);

        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals("Items list must be empty for a transaction with no items",
                0, result.getItems().size());
    }

    // =========================================================================
    // getTransactionsByStatus
    // =========================================================================

    /**
     * getTransactionsByStatus("COMPLETED") must return only completed transactions.
     */
    @Test
    public void getTransactionsByStatus_returnsOnlyMatchingTransactions()
            throws InterruptedException {
        transactionDao.insertTransaction(makeTransaction("TRX-A1", "COMPLETED", 10_000L));
        transactionDao.insertTransaction(makeTransaction("TRX-A2", "COMPLETED", 20_000L));
        transactionDao.insertTransaction(makeTransaction("TRX-A3", "PENDING",   15_000L));

        List<Transaction> completed =
                getOrAwaitValue(transactionDao.getTransactionsByStatus("COMPLETED"));

        assertNotNull(completed);
        assertEquals("Must return only 2 COMPLETED transactions", 2, completed.size());
        for (Transaction t : completed) {
            assertEquals("Status must be COMPLETED", "COMPLETED", t.getStatus());
        }
    }

    /**
     * getTransactionsByStatus for a status with no records must emit an empty list.
     */
    @Test
    public void getTransactionsByStatus_noMatchingStatus_returnsEmptyList()
            throws InterruptedException {
        transactionDao.insertTransaction(makeTransaction("TRX-B1", "COMPLETED", 5_000L));

        List<Transaction> pending =
                getOrAwaitValue(transactionDao.getTransactionsByStatus("PENDING"));

        assertNotNull(pending);
        assertEquals("Must return empty list when no match", 0, pending.size());
    }

    /**
     * getTransactionsByStatus("PENDING") must not include COMPLETED transactions.
     */
    @Test
    public void getTransactionsByStatus_pendingQuery_doesNotReturnCompleted()
            throws InterruptedException {
        transactionDao.insertTransaction(makeTransaction("TRX-C1", "PENDING",   8_000L));
        transactionDao.insertTransaction(makeTransaction("TRX-C2", "COMPLETED", 12_000L));

        List<Transaction> pending =
                getOrAwaitValue(transactionDao.getTransactionsByStatus("PENDING"));

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
    public void updateTransactionStatus_statusIsChanged() throws InterruptedException {
        Transaction transaction = makeTransaction("TRX-D1", "PENDING", 30_000L);
        long id = transactionDao.insertTransaction(transaction);

        transactionDao.updateTransactionStatus(id, "COMPLETED");

        List<Transaction> completed =
                getOrAwaitValue(transactionDao.getTransactionsByStatus("COMPLETED"));

        assertNotNull(completed);
        assertFalse("Completed list must not be empty after status update", completed.isEmpty());

        boolean found = false;
        for (Transaction t : completed) {
            if (t.getTransactionCode().equals("TRX-D1")) {
                found = true;
                assertEquals("Status must be COMPLETED", "COMPLETED", t.getStatus());
            }
        }
        assertEquals("TRX-D1 must appear in COMPLETED list", true, found);
    }

    /**
     * updateTransactionStatus must not change the status of other transactions.
     */
    @Test
    public void updateTransactionStatus_doesNotAffectOtherTransactions()
            throws InterruptedException {
        long idToUpdate = transactionDao.insertTransaction(
                makeTransaction("TRX-E1", "PENDING", 5_000L));
        transactionDao.insertTransaction(
                makeTransaction("TRX-E2", "PENDING", 10_000L));

        transactionDao.updateTransactionStatus(idToUpdate, "COMPLETED");

        List<Transaction> stillPending =
                getOrAwaitValue(transactionDao.getTransactionsByStatus("PENDING"));

        assertNotNull(stillPending);
        assertEquals("Only TRX-E2 should remain PENDING", 1, stillPending.size());
        assertEquals("TRX-E2", stillPending.get(0).getTransactionCode());
    }
}
