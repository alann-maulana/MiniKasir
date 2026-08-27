package id.my.alan.minikasir.ui.product;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.ProductEntity;
import id.my.alan.minikasir.data.repository.ProductRepository;
import id.my.alan.minikasir.data.repository.TransactionRepository;

public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<ProductEntity>> products;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.productRepository = new ProductRepository(db);
        this.transactionRepository = new TransactionRepository(db);

        // Switch between all products and search query
        this.products = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return productRepository.getAllProducts();
            } else {
                return productRepository.searchProducts(query.trim());
            }
        });
    }

    public LiveData<List<ProductEntity>> getProducts() {
        return products;
    }

    public LiveData<Integer> getPendingSyncCount() {
        return transactionRepository.getPendingCount();
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void insertProduct(ProductEntity product) {
        productRepository.insertProduct(product);
    }

    public void updateProduct(ProductEntity product) {
        productRepository.updateProduct(product);
    }

    public void deleteProduct(ProductEntity product) {
        productRepository.deleteProduct(product);
    }
}
