package coms3620.fashion.departments.logistics.order;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import coms3620.fashion.departments.logistics.Product;
import coms3620.fashion.departments.logistics.Status;
import coms3620.fashion.departments.logistics.Trackable;

/**
 * @author Joseph Hennings
 */
public class Order implements Trackable {
    private final String id;
    private final List<OrderLine> orderLines;
    private Status status;
    private Map<String, Integer> productQuantities = new HashMap<>();


    public Order(String id, List<OrderLine> orderLines) {
        this.id = id;
        this.orderLines = orderLines;
        this.status = Status.PENDING;
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

    public void ship() {
        this.status = Status.SHIPPED;
    }

    public void finalizeOrder() {
        productQuantities.clear();
        for (OrderLine ol : orderLines) {
            productQuantities.merge(ol.getProductSku(), ol.getQuantity(), Integer::sum);
        }
    }

    public String generateSummary() {
        HashSet<String> seen = new HashSet<>();
        StringBuilder sb = new StringBuilder();

        sb.append("=========================================== Order Summary ============================================\n")
        .append("Order ID: ").append(id).append("\n")
        .append("Status: ").append(status).append("\n")
        .append("------------------------------------------------------------------------------------------------------\n");

        if (productQuantities.isEmpty()) {
            sb.append("  (No products added or order not finalized)\n")
            .append("======================================================================================================\n");
            return sb.toString();
        }

        sb.append(String.format(
                "%-40s %-25s %-10s %-12s %-12s\n",
                "Product", "SKU", "Qty", "Price", "Line Total"))
        .append("------------------------------------------------------------------------------------------------------\n");

        int totalQuantity = 0;
        double totalCost = 0.0;

        for (OrderLine ol : orderLines) {
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

        sb.append("------------------------------------------------------------------------------------------------------\n")
        .append(String.format(
                "%-40s %-25s %-10s %-12s $%-11.2f\n",
                "Totals:", "", totalQuantity, "", totalCost))
        .append("======================================================================================================\n");

        return sb.toString();
    }

    public List<OrderLine> getOrderLines() {
        return this.orderLines;
    }
}