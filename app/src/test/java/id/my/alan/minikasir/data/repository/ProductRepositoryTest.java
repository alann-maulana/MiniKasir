package id.my.alan.minikasir.data.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import id.my.alan.minikasir.data.local.dao.ProductDao;
import id.my.alan.minikasir.data.local.db.AppDatabase;
import id.my.alan.minikasir.data.local.entity.ProductEntity;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductRepositoryTest {

    @Mock
    private AppDatabase db;

    @Mock
    private ProductDao productDao;

    private ProductRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(db.productDao()).thenReturn(productDao);
        repository = new ProductRepository(db);
    }

    @Test
    public void getAllProducts_callsDao() {
        repository.getAllProducts();
        verify(productDao).getAllProducts();
    }

    @Test
    public void searchProducts_callsDaoWithWildcards() {
        repository.searchProducts("kopi");
        verify(productDao).searchProducts("%kopi%");
    }
}
