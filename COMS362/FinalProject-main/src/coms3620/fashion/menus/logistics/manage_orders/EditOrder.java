package coms3620.fashion.menus.logistics.manage_orders;

import java.util.List;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.departments.logistics.Product;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.Stdin;
import coms3620.fashion.departments.logistics.order.Order;
import coms3620.fashion.departments.logistics.order.OrderLine;

/**
 * @author Joseph Hennings
 */
public class EditOrder implements Option {
    private LogisticsManager logisticsManager;

    public EditOrder(LogisticsManager logisticsManager) {
        this.logisticsManager = logisticsManager;
    }

    @Override
    public String getName() {
        return "Edit order";
    }

    private void printOrderTable(Order order) {
        List<OrderLine> lines = order.getOrderLines();

        System.out.printf("%-4s %-35s %-20s %-10s %-12s %-14s\n",
                "#", "Product", "SKU", "Qty", "Price", "Line Total");
        System.out.println("------------------------------------------------------------------------------------------------");

        int index = 1;
        for (OrderLine ol : lines) {
            Product p = ol.getProduct();
            int qty = ol.getQuantity();
            double price = p.getPrice();
            double lineTotal = qty * price;

            System.out.printf("%-4d %-35s %-20s %-10d $%-11.2f $%-13.2f\n",
                index++,
                p.getName(),
                p.getSKU(),
                qty,
                price,
                lineTotal
            );
        }

        System.out.println("================================================================================================\n");
    }

    private void addProducts(Order order) {
        System.out.println();
        new ViewProducts(logisticsManager).run();
        AddProduct addProduct = new AddProduct(logisticsManager);

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

        addProduct.addProductToOrder(matches, order.getOrderLines());
    }

    private void editQuantities(Order order) {
        List<OrderLine> orderLines = order.getOrderLines();

        if (orderLines.isEmpty()) {
            System.out.println("This order has no products to edit");
            return;
        }

        System.out.println("======================================= Edit Quantities ========================================");
        printOrderTable(order);

        System.out.print("Select product number to edit --> ");
        int choice = Stdin.nextInt();
        System.out.println();

        while (choice < 1 || choice > orderLines.size()) {
            System.out.print("Invalid choice. Try again --> ");
            choice = Stdin.nextInt();
            System.out.println();
        }

        OrderLine selected = orderLines.get(choice - 1);
        Product product = selected.getProduct();

        int oldQty = selected.getQuantity();

        System.out.println("Editing: " + product.getName() + " (Current qty: " + oldQty + ")");
        System.out.print("Enter new quantity (0 = remove) --> ");
        int newQty = Stdin.nextInt();
        System.out.println();

        while (newQty < 0) {
            System.out.print("Invalid. Enter qty >= 0 --> ");
            newQty = Stdin.nextInt();
            System.out.println();
        }

        if (newQty == oldQty) {
            System.out.println("Quantity unchanged.\n");
            return;
        }

        // Decreasing --> returning product stock
        if (newQty < oldQty) {
            int returned = oldQty - newQty;
            logisticsManager.increaseProductQuantity(product.getSKU(), returned);

             if (newQty == 0) {
                orderLines.remove(choice - 1);
                System.out.println("Item removed from order.\n");
            } else {
                selected.setQuantity(newQty);
                System.out.println("Quantity decreased.\n");
            }

            order.finalizeOrder();
            return;
        }

        // Increasing --> reducing product stock
        if (newQty > oldQty) {
            int needed = newQty - oldQty;

            while (!logisticsManager.reduceProductQuantity(product.getSKU(), needed)) {
                System.out.print("Insufficient stock. Enter new quantity --> ");
                newQty = Stdin.nextInt();
                System.out.println();
                needed = newQty - oldQty;

                if (newQty < 0)
                    needed = 0;
            }
            selected.setQuantity(newQty);
            System.out.println("Quantity increased.");
        }
        order.finalizeOrder();
    }

    public void editOrder(Order order) {
        System.out.println("What would you like to do?");
        System.out.println("1 = edit quantities");
        System.out.println("2 = add new product(s)");
        System.out.print("-- > ");
        int choice = Stdin.nextInt();
        System.out.println();

        while (choice != 1 && choice != 2) {
            System.out.print("Invalid choice, try again --> ");
            choice = Stdin.nextInt();
            System.out.println();
        }

        switch (choice) {
            case 1:
                editQuantities(order);
                break;
            case 2:
                boolean keepAdding = true;
                do {
                    addProducts(order);

                    System.out.println("Add another product?");
                    System.out.print("[Y]es / [N]o --> ");
                    char keepAddingChoice = Stdin.nextLine().charAt(0);
                    keepAdding = keepAddingChoice == 'y' || keepAddingChoice == 'Y';
                    System.out.println();
                } while (keepAdding);
                order.finalizeOrder();
                break;
        }
    }

    @Override
    public void run() {
        new ViewOrders(logisticsManager).run();
        List<Order> orders = logisticsManager.getOrders();

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
            editOrder(order);
            logisticsManager.saveProducts();
        }
    }
    
}
