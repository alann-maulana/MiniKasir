package id.my.alan.minikasir.domain.usecase;

import java.util.ArrayList;
import java.util.List;

import id.my.alan.minikasir.data.local.entity.TransactionEntity;
import id.my.alan.minikasir.data.local.entity.TransactionItemEntity;
import id.my.alan.minikasir.data.repository.TransactionRepository;
import id.my.alan.minikasir.domain.model.CartItem;
import id.my.alan.minikasir.util.DateUtils;

/**
 * UseCase encapsulating the business rules for creating a POS transaction.
 */
public class CreateTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public CreateTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Validates cart items, generates transaction entity and line items, and persists them via repository.
     *
     * @param cartItems List of items in the cart
     * @param note      Optional note from cashier
     * @return Generated transaction code
     * @throws IllegalArgumentException if cart is empty or contains invalid items
     */
    public String execute(List<CartItem> cartItems, String note) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Keranjang belanja tidak boleh kosong");
        }

        long totalAmount = 0;
        int totalItemCount = 0;

        for (CartItem item : cartItems) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Jumlah item '" + item.getProductName() + "' harus lebih dari 0");
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Harga item '" + item.getProductName() + "' tidak valid");
            }
            totalAmount += item.getSubtotal();
            totalItemCount += item.getQuantity();
        }

        String transactionCode = DateUtils.generateTransactionCode();
        long now = System.currentTimeMillis();

        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransactionCode(transactionCode);
        transaction.setTotalAmount(totalAmount);
        transaction.setItemCount(totalItemCount);
        transaction.setStatus(TransactionRepository.STATUS_PENDING);
        transaction.setNote(note != null ? note.trim() : null);
        transaction.setCreatedAt(now);
        transaction.setSyncedAt(0);

        List<TransactionItemEntity> itemEntities = new ArrayList<>();
        for (CartItem item : cartItems) {
            TransactionItemEntity entity = new TransactionItemEntity();
            entity.setProductId(item.getProductId());
            entity.setProductName(item.getProductName());
            entity.setUnitPrice(item.getUnitPrice());
            entity.setQuantity(item.getQuantity());
            entity.setSubtotal(item.getSubtotal());
            itemEntities.add(entity);
        }

        transactionRepository.createTransaction(transaction, itemEntities);
        return transactionCode;
    }
}
