package id.my.alan.minikasir.domain.model;

import java.util.Objects;

/**
 * Model representing an item in the shopping cart during a POS transaction session.
 */
public class CartItem {
    private final long productId;
    private final String productName;
    private final long unitPrice;
    private int quantity;

    public CartItem(long productId, String productName, long unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    public long getSubtotal() {
        return unitPrice * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return productId == cartItem.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
