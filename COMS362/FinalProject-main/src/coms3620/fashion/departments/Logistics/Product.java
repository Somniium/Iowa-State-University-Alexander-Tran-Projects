package coms3620.fashion.departments.logistics;

/**
 * @author Joseph Hennings
 */
public class Product {
    private final String name;
    private final String sku;
    private final String size;
    private final double price;
    private int quantity; // mutable

    public Product(String name, String sku, String size, double price, int quantity) {
        this.name = name;
        this.sku = sku;
        this.size = size;
        this.price = price;
        this.quantity = quantity;
    }

    public boolean reduceQuantity(int amount) {
        if (amount < 0 || amount > quantity)
            return false;
        else {
            quantity -= amount;
            return true;
        }
    }

    public void increaseQuantity(int amount) {
        if (amount > 0)
            quantity += amount;
    }

    public String getName() {
        return name;
    }

    public String getSKU() {
        return sku;
    }

    public String getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity; }
}
