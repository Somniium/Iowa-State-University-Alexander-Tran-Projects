package coms3620.fashion.menus.logistics.manage_shipments;

import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.order.Order;
import coms3620.fashion.departments.logistics.shipment.Shipment;
import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;
import coms3620.fashion.menus.logistics.manage_orders.EditOrder;
import coms3620.fashion.menus.logistics.manage_orders.ViewOrders;
import coms3620.fashion.util.Stdin;

/**
 * @author Joseph Hennings
 */
public class EditShipment extends Menu implements Option  {
    private LogisticsManager logisticsManager;

    public EditShipment(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;

    }

    @Override
    public String getName() {
        return "Edit shipment";
    }

    private void editShipment(Shipment shipment) {
        List<Order> orders = shipment.getOrders();
        new ViewOrders(logisticsManager).run(orders);

        if (!orders.isEmpty()) {
            System.out.print("Enter order no. to edit --> ");
            int choice = Stdin.nextInt();
            System.out.println();

            while (choice > orders.size() || choice < 1) {
                System.out.print("Invalid order no., try again --> ");
                choice = Stdin.nextInt();
                System.out.println();
            }

            Order order = orders.get(choice - 1);
            new EditOrder(logisticsManager).editOrder(order);
        }
    }

    @Override
    public void run() {
        new ViewShipments(logisticsManager).run();
        List<Shipment> shipments = logisticsManager.getShipments();

        if (!shipments.isEmpty()) {
            System.out.print("Enter shipment no. to edit --> ");
            int choice = Stdin.nextInt();
            System.out.println();

            while (choice > shipments.size() || choice < 1) {
                System.out.print("Invalid shipment no., try again --> ");
                choice = Stdin.nextInt();
                System.out.println();
            }

            Shipment shipment = shipments.get(choice - 1);
            editShipment(shipment);
        }
    }
}
