package id.my.alan.minikasir.domain.usecase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import id.my.alan.minikasir.data.repository.TransactionRepository;
import id.my.alan.minikasir.domain.model.CartItem;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

public class CreateTransactionUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    private CreateTransactionUseCase useCase;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new CreateTransactionUseCase(transactionRepository);
    }

    @Test
    public void execute_withValidCartItems_createsTransactionSuccessfully() {
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new CartItem(1L, "Nasi Goreng", 25000L, 2));
        cartItems.add(new CartItem(2L, "Es Teh Manis", 5000L, 1));

        String code = useCase.execute(cartItems, "Meja 3");

        assertNotNull(code);
        assertTrue(code.startsWith("TRX-"));
        verify(transactionRepository).createTransaction(any(), anyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_withEmptyCart_throwsException() {
        useCase.execute(new ArrayList<>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_withNullCart_throwsException() {
        useCase.execute(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void execute_withZeroQuantityItem_throwsException() {
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new CartItem(1L, "Nasi Goreng", 25000L, 0));

        useCase.execute(cartItems, null);
    }
}
