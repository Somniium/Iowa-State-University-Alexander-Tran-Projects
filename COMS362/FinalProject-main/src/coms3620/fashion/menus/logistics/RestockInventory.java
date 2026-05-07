package coms3620.fashion.menus.logistics;

import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.Product;
import coms3620.fashion.menus.Option;
import coms3620.fashion.menus.logistics.manage_orders.ViewProducts;
import coms3620.fashion.util.Stdin;

/**
 * @author Joseph Hennings
 */
public class RestockInventory implements Option {
    private LogisticsManager logisticsManager;

    public RestockInventory(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "Restock Inventory";
    }

    @Override
    public void run() {
        System.out.println();
        new ViewProducts(logisticsManager).run();

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

        if (matches.size() == 1)
            System.out.println("1 product found:");
        else
            System.out.println(matches.size() + " products found:");

        new ViewProducts(logisticsManager).productFormatter(matches);

        System.out.print("Choose product no. --> ");
        int choice = Stdin.nextInt();
        System.out.println();

        while (choice <= 0 || choice > matches.size()) {
            System.out.print("Invalid. Please try again --> ");
            choice = Stdin.nextInt();
            System.out.println();
        }

        Product p = matches.get(choice - 1);

        System.out.print("Enter amount to restock --> ");
        int amount = Stdin.nextInt();
        System.out.println();

        while (amount < 0) {
            System.out.print("Invalid quantity, try again --> ");
            amount = Stdin.nextInt();
            System.out.println();
        }

        if (amount == 0) {
            System.out.println("Product quantity not changed.");
        }
        else {
            logisticsManager.increaseProductQuantity(p.getSKU(), amount);
            System.out.println("Product quantity increased. New product quantity: " + p.getQuantity());
        }

    }
}
