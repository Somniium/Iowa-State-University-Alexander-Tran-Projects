package coms3620.fashion.departments.logistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import coms3620.fashion.departments.logistics.order.Order;
import coms3620.fashion.departments.logistics.order.OrderLine;
import coms3620.fashion.departments.logistics.shipment.Shipment;
import coms3620.fashion.util.RandStringGenerator;

/**
 * @author Joseph Hennings
 */
public class LogisticsManager {
    private List<Order> orders;
    private List<Shipment> shipments;
    private final ProductRepository productRepository;
    private final RandStringGenerator randString;

    public LogisticsManager() {
        orders = new ArrayList<>();
        shipments = new ArrayList<>();
        productRepository = new ProductRepository("data/logistics/products.csv");
        randString = new RandStringGenerator();
    }

    public Order createOrder(List<OrderLine> ols) {
        Order order = new Order(randString.generateRandomString(8), ols);
        order.finalizeOrder();
        orders.add(order);
        saveProducts();

        return order;
    }

    public Shipment createShipment() {
        for (Order order : orders)
                order.updateStatus(Status.EXPEDITED);
        String id = randString.generateRandomString(8);
        Shipment shipment = new Shipment(new ArrayList<>(orders), id);
        shipments.add(shipment);
        orders.clear();

        return shipment;
    }

    public void cancelShipment(Shipment shipment) {
        for (Order order : shipment.getOrders()) {
            order.updateStatus(Status.PENDING);
            this.orders.add(order);
        }
        shipments.remove(shipment);
    }

    public void saveProducts() {
        productRepository.save();
    }

    public boolean containsProduct(String name) {
        return productRepository.containsProduct(name);
    }

    public boolean reduceProductQuantity(String sku, int amount) {
        return productRepository.reduceProductQuantity(sku, amount);
    }

    public void increaseProductQuantity(String sku, int amount) {
        productRepository.increaseProductQuantity(sku, amount);
    }

    public List<Order> getOrders() {
        return this.orders;
    }

    public List<Shipment> getShipments() {
        return this.shipments;
    }

    public Collection<Product> getAllProducts() {
        return productRepository.getAll();
    }

    public List<Product> findProductsByName(String keyWord) {
        return productRepository.getProductByName(keyWord);
    }
}