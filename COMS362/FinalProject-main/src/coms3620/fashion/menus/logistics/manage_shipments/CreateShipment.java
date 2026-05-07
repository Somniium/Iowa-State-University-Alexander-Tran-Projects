package coms3620.fashion.menus.logistics.manage_shipments;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.shipment.Shipment;
import coms3620.fashion.menus.Option;
import coms3620.fashion.menus.logistics.manage_orders.ViewOrders;
import coms3620.fashion.util.Stdin;

/**
 * @author Joseph Hennings
 */
public class CreateShipment implements Option {
    private LogisticsManager logisticsManager;

    public CreateShipment(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "Create Shipment";
    }

    @Override
    public void run() {
        new ViewOrders(logisticsManager).run();
        if (logisticsManager.getOrders().isEmpty())
            return;
        else {
            System.out.println("Ship these orders?");
            System.out.print("[Y]es / [N]o --> ");
            char shipChoice = Stdin.nextLine().charAt(0);
            boolean ship = shipChoice == 'Y' || shipChoice == 'y';
            if (!ship)
                System.out.println("Shipment was not made.");
            else {
                Shipment shipment = logisticsManager.createShipment();
                System.out.println("New shipment was successfully made, id: " + shipment.getID());
            }
        }
    }
}
