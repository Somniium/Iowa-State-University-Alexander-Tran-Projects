package coms3620.fashion.menus.logistics.manage_orders;

import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.Product;
import coms3620.fashion.departments.logistics.order.OrderLine;
import coms3620.fashion.util.Stdin;

/**
 * @author Joseph Hennings
 * HELPER CLASS
 */
public class AddProduct {
    private LogisticsManager logisticsManager;

    public AddProduct(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    public void addProductToOrder(List<Product> products, List<OrderLine> orderLines) {
        if (products.size() == 1)
            System.out.println("1 product found:");
        else
            System.out.println(products.size() + " products found:");

        new ViewProducts(logisticsManager).productFormatter(products);

        System.out.print("Choose product no. --> ");
        int choice = Stdin.nextInt();
        System.out.println();

        while (choice <= 0 || choice > products.size()) {
            System.out.print("Invalid. Please try again --> ");
            choice = Stdin.nextInt();
            System.out.println();
        }

        Product p = products.get(choice - 1);

        System.out.print("Enter product quantity --> ");
        int quantity = Stdin.nextInt();
        System.out.println();

        while (!logisticsManager.reduceProductQuantity(p.getSKU(), quantity)) {
            System.out.print("Insufficient stock, only " + p.getQuantity() + " remaining. please try again --> ");
            quantity = Stdin.nextInt();
            System.out.println();
        }
        if (quantity != 0)
            orderLines.add(new OrderLine(p, quantity));
        else {
            System.out.println("Product was not added.");
            System.out.println();
        }
    }
}
