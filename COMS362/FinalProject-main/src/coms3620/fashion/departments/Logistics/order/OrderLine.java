package coms3620.fashion.departments.logistics.order;

import coms3620.fashion.departments.logistics.Product;

/**
 * @author Joseph Hennings
 */
public class OrderLine {

    private final Product product;
    private int quantity;

    public OrderLine(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return this.product;
    }

    public String getProductName() {
        return product.getName();
    }

    public String getProductSku() {
        return product.getSKU();
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void refundQuantity() {
        this.product.increaseQuantity(quantity);
        this.quantity = 0;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
