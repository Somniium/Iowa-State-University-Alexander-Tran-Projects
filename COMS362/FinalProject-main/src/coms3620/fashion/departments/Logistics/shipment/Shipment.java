package coms3620.fashion.departments.logistics.shipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import coms3620.fashion.departments.logistics.Product;
import coms3620.fashion.departments.logistics.Status;
import coms3620.fashion.departments.logistics.Trackable;
import coms3620.fashion.departments.logistics.order.Order;
import coms3620.fashion.departments.logistics.order.OrderLine;

/**
 * @author Joseph Hennings
 */
public class Shipment implements Trackable {
    private String id;
    private List<Order> orders = new ArrayList<>();
    private Status status;
    private static Map<String, Integer> productQuantities = new HashMap();

    public Shipment(List<Order> orders, String id) {
        this.orders = orders;
        this.id = id;
        status = Status.PENDING;
    }

    private void compileQuantities() {
        productQuantities.clear();
        for (Order order : orders) {
            for (OrderLine ol : order.getOrderLines()) {
                productQuantities.merge(ol.getProductSku(), ol.getQuantity(), Integer::sum);
            }
        }
    }

    public void ship() {
        this.status = Status.SHIPPED;
        for (Order order : orders) 
            order.ship();
    }

    public List<Order> getOrders() {
        return this.orders;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public String getStatus() {
        return status.toString();
    }

    @Override
    public void updateStatus(Status status) {
        this.status = status;
    }

    public String generateInvoice() {
        compileQuantities();
        HashSet<String> seen = new HashSet<>();
        StringBuilder sb = new StringBuilder();

        sb.append("========================================== Shipment Invoice ==========================================\n")
        .append("Shipment ID: ").append(id).append("\n")
        .append("Status: ").append(status).append("\n")
        .append("Orders Included: ");

        for (Order order : orders)
            sb.append(order.getID()).append(" ");

        sb.append("\n------------------------------------------------------------------------------------------------------\n");

        if (productQuantities.isEmpty()) {
            sb.append("  (No products added or shipment not finalized)\n")
            .append("======================================================================================================\n");
            return sb.toString();
        }

        sb.append(String.format(
            "%-40s %-25s %-10s %-12s %-12s\n",
            "Product", "SKU", "Qty", "Price", "Line Total"
        ));

        sb.append("------------------------------------------------------------------------------------------------------\n");

        int totalQuantity = 0;
        double totalCost = 0.0;

        for (Order order : orders) {
            for (OrderLine ol : order.getOrderLines()) {
                Product p = ol.getProduct();
                String sku = p.getSKU();

                if (productQuantities.containsKey(sku) && !seen.contains(sku)) {
                    int qty = productQuantities.get(sku);
                    double price = p.getPrice();
                    double lineTotal = qty * price;

                    sb.append(String.format(
                        "%-40s %-25s %-10d $%-11.2f $%-11.2f\n",
                        p.getName(),
                        p.getSKU(),
                        qty,
                        price,
                        lineTotal
                    ));

                    totalQuantity += qty;
                    totalCost += lineTotal;
                    seen.add(sku);
                }
            }
        }

        sb.append("------------------------------------------------------------------------------------------------------\n")
        .append(String.format(
            "%-40s %-25s %-10s %-12s $%-11.2f\n",
            "Totals:", "", totalQuantity, "", totalCost
        ))
        .append("======================================================================================================\n");

        return sb.toString();
    }
}
