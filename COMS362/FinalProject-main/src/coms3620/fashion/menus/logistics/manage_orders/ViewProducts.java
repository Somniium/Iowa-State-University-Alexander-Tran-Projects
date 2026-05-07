package coms3620.fashion.menus.logistics.manage_orders;

import java.util.ArrayList;
import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.Product;
import coms3620.fashion.menus.Option;

/**
 * @author Joseph Hennings
 */
public class ViewProducts implements Option {
    private LogisticsManager logisticsManager;

    public ViewProducts(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "View Products";
    }

    public void productFormatter(List<Product> products) {
        System.out.printf(
            "%-5s %-30s %-20s %-10s %-10s %-10s\n",
            "#", "Name", "SKU", "Size", "Price", "Stock"
        );
        System.out.println("-------------------------------------------------------------------------------------");

        int index = 1;
        for (Product p : products) {
            productFormatter(p, index++);
        }

        System.out.println("====================================================================================");
    }

    private void productFormatter(Product p, int index) {
        System.out.printf(
            "%-5s %-30s %-20s %-10s $%-9.2f %-10d\n",
            index + ")",   
            p.getName(),  
            p.getSKU(),   
            p.getSize(),
            p.getPrice(),  
            p.getQuantity()
        );
    }


    @Override
    public void run() {
        System.out.println("\n================================ Available Products =================================");
        productFormatter(new ArrayList<>(logisticsManager.getAllProducts()));
    }

}
