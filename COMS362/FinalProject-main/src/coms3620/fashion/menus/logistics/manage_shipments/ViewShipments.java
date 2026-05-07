package coms3620.fashion.menus.logistics.manage_shipments;

import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.shipment.Shipment;
import coms3620.fashion.menus.Option;

/**
 * @author Joseph Hennings
 */
public class ViewShipments implements Option {
    private LogisticsManager logisticsManager;

    public ViewShipments(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "View shipments";
    }

    @Override
    public void run() {
        System.out.println();
        List<Shipment> shipments = logisticsManager.getShipments();
        if (shipments.isEmpty()) {
            System.out.println("There are currently no shipments.");
            return;
        }
        else {
            int index = 1;
            for (Shipment shipment : shipments) {
                System.out.println("Shipment no. " + index);
                System.out.println(shipment.generateInvoice());
                index++;
            }
        }
    }
}
