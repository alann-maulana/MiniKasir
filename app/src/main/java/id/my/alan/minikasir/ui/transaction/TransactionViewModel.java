package id.my.alan.minikasir.ui.transaction;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.ProductEntity;
import id.my.alan.minikasir.data.local.entity.TransactionEntity;
import id.my.alan.minikasir.data.repository.ProductRepository;
import id.my.alan.minikasir.data.repository.TransactionRepository;
import id.my.alan.minikasir.domain.model.CartItem;
import id.my.alan.minikasir.domain.usecase.CreateTransactionUseCase;

public class TransactionViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final CreateTransactionUseCase createTransactionUseCase;

    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Long> cartTotal = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> cartItemCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> checkoutSuccessEvent = new MutableLiveData<>();
    private final MutableLiveData<String> checkoutErrorEvent = new MutableLiveData<>();

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.productRepository = new ProductRepository(db);
        this.transactionRepository = new TransactionRepository(db);
        this.createTransactionUseCase = new CreateTransactionUseCase(transactionRepository);
    }

    public LiveData<List<ProductEntity>> getAllProducts() {
        return productRepository.getAllProducts();
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return transactionRepository.getAllTransactions();
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<Long> getCartTotal() {
        return cartTotal;
    }

    public LiveData<Integer> getCartItemCount() {
        return cartItemCount;
    }

    public LiveData<String> getCheckoutSuccessEvent() {
        return checkoutSuccessEvent;
    }

    public LiveData<String> getCheckoutErrorEvent() {
        return checkoutErrorEvent;
    }

    public void addToCart(ProductEntity product) {
        List<CartItem> current = new ArrayList<>(cartItems.getValue() != null ? cartItems.getValue() : new ArrayList<>());
        boolean found = false;
        for (CartItem item : current) {
            if (item.getProductId() == product.getId()) {
                item.incrementQuantity();
                found = true;
                break;
            }
        }
        if (!found) {
            current.add(new CartItem(product.getId(), product.getName(), product.getPrice(), 1));
        }
        updateCartState(current);
    }

    public void incrementCartItem(long productId) {
        List<CartItem> current = new ArrayList<>(cartItems.getValue() != null ? cartItems.getValue() : new ArrayList<>());
        for (CartItem item : current) {
            if (item.getProductId() == productId) {
                item.incrementQuantity();
                break;
            }
        }
        updateCartState(current);
    }

    public void decrementCartItem(long productId) {
        List<CartItem> current = new ArrayList<>(cartItems.getValue() != null ? cartItems.getValue() : new ArrayList<>());
        for (int i = 0; i < current.size(); i++) {
            CartItem item = current.get(i);
            if (item.getProductId() == productId) {
                if (item.getQuantity() > 1) {
                    item.decrementQuantity();
                } else {
                    current.remove(i);
                }
                break;
            }
        }
        updateCartState(current);
    }

    public void removeFromCart(long productId) {
        List<CartItem> current = new ArrayList<>(cartItems.getValue() != null ? cartItems.getValue() : new ArrayList<>());
        current.removeIf(item -> item.getProductId() == productId);
        updateCartState(current);
    }

    public void clearCart() {
        updateCartState(new ArrayList<>());
    }

    public void checkout(String note) {
        List<CartItem> current = cartItems.getValue();
        try {
            String code = createTransactionUseCase.execute(current, note);
            clearCart();
            checkoutSuccessEvent.postValue(code);
        } catch (IllegalArgumentException e) {
            checkoutErrorEvent.postValue(e.getMessage());
        }
    }

    private void updateCartState(List<CartItem> current) {
        cartItems.setValue(current);
        long total = 0;
        int count = 0;
        for (CartItem item : current) {
            total += item.getSubtotal();
            count += item.getQuantity();
        }
        cartTotal.setValue(total);
        cartItemCount.setValue(count);
    }
}
