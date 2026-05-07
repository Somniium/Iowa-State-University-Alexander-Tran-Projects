package coms3620.fashion.menus.logistics.manage_shipments;

import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.shipment.Shipment;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.Stdin;

/**
 * @author Joseph Hennings
 */
public class CancelShipment implements Option {
    private LogisticsManager logisticsManager;

    public CancelShipment(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "Cancel shipments";
    }

    @Override
    public void run() {
        new ViewShipments(logisticsManager).run();
        List<Shipment> shipments = logisticsManager.getShipments();

        if (!shipments.isEmpty()) {
            System.out.print("Enter shipment no. to Cancel --> ");
            int choice = Stdin.nextInt();
            System.out.println();

            while (choice > shipments.size() || choice < 1) {
                System.out.print("Invalid shipment no., try again --> ");
                choice = Stdin.nextInt();
                System.out.println();
            }

            Shipment shipment = shipments.get(choice - 1);
            logisticsManager.cancelShipment(shipment);
        }
    }
}
