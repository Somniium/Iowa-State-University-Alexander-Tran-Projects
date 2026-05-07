package coms3620.fashion.menus.logistics.manage_orders;

import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.order.Order;
import coms3620.fashion.departments.logistics.order.OrderLine;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.Stdin;

/**
 * @author Joseph Hennings
 */
public class DeleteOrder implements Option {
    private LogisticsManager logisticsManager;

    public DeleteOrder(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "Delete order";
    }

    @Override
    public void run() {
        System.out.println();
        List<Order> orders = logisticsManager.getOrders();
        if (orders.isEmpty()) {
            System.out.println("There are currently no orders to delete.");
            return;
        }
        new ViewOrders(logisticsManager).run();
        System.out.print("Enter order number --> ");
        int index = Stdin.nextInt();
        if (index > orders.size())
            System.out.println("Order was not deleted - no such order.");
        else {
            Order order = orders.get(index - 1);
            List<OrderLine> orderLines = order.getOrderLines();

            for (OrderLine orderLine : orderLines)            
                orderLine.refundQuantity();
            
            logisticsManager.saveProducts();
            orders.remove(index - 1);
            System.out.println("Order no. " + index + " was successfully deleted.");
        }

    }
}
