package coms3620.fashion.menus.logistics.manage_orders;

import java.util.ArrayList;
import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.Product;
import coms3620.fashion.departments.logistics.order.Order;
import coms3620.fashion.departments.logistics.order.OrderLine;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.Stdin;

/**
 * @author Joseph Hennings
 */
public class CreateOrder implements Option {
    private LogisticsManager logisticsManager;

    public CreateOrder(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "Create order";
    }

    @Override
    public void run() {
        System.out.println();
        new ViewProducts(logisticsManager).run();
        AddProduct addProduct = new AddProduct(logisticsManager);
        List<OrderLine> orderLines = new ArrayList<>();
        boolean keepAdding = true;

        do {
            System.out.print("Enter keyword(s) --> ");
            String keyword = Stdin.nextLine();
            System.out.println();

            List<Product> matches = logisticsManager.findProductsByName(keyword);

            while (matches.isEmpty()) {
                System.out.print("No products match that keyword, please try again --> ");
                keyword = Stdin.nextLine();
                System.out.println();
                matches = logisticsManager.findProductsByName(keyword);
            }

            addProduct.addProductToOrder(matches, orderLines);

            System.out.println("Add another product?");
            System.out.print("[Y]es / [N]o --> ");
            char keepAddingChoice = Stdin.nextLine().charAt(0);
            keepAdding = keepAddingChoice == 'y' || keepAddingChoice == 'Y';
            System.out.println();

        } while(keepAdding);

        if (orderLines.size() > 0) {
            Order order = logisticsManager.createOrder(orderLines);
            System.out.println("New order was successfully made, id: " + order.getID());
        }
        else {
            System.out.println("Order was not made.");
        }
    }
}
