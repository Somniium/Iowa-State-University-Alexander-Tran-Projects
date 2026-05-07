package coms3620.fashion.menus.logistics.manage_orders;

import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.order.Order;
import coms3620.fashion.menus.Option;

/**
 * @author Joseph Hennings
 */
public class ViewOrders implements Option {
    private LogisticsManager logisticsManager;

    public ViewOrders(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "View orders";
    }

    public void run(List<Order> orders) {
        System.out.println();
        if (orders.isEmpty())
            System.out.println("There are currently no orders.");
        else {
            int index = 1;
            for (Order order : orders) {
                System.out.println("Order no. " + index);
                System.out.println(order.generateSummary());
                index++;
            }
        }
    }

    @Override
    public void run() {
        System.out.println();
        List<Order> orders = logisticsManager.getOrders();
        if (orders.isEmpty())
            System.out.println("There are currently no orders.");
        else {
            int index = 1;
            for (Order order : orders) {
                System.out.println("Order no. " + index);
                System.out.println(order.generateSummary());
                index++;
            }
        }
    }
}
