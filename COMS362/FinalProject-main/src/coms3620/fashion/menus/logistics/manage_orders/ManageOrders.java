package coms3620.fashion.menus.logistics.manage_orders;

import coms3620.fashion.departments.logistics.LogisticsManager;
import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;

/**
 * @author Joseph Hennings
 */
public class ManageOrders extends Menu implements Option {

    public ManageOrders(LogisticsManager lm) {
        CreateOrder createOrder = new CreateOrder(lm);
        ViewOrders viewOrders = new ViewOrders(lm);
        DeleteOrder deleteOrder = new DeleteOrder(lm);
        EditOrder editOrder = new EditOrder(lm);

        addOption(createOrder);
        addOption(viewOrders);
        addOption(deleteOrder);
        addOption(editOrder);
    }

    @Override
    public String getName() {
        return "Manage Orders";
    }

    @Override
    public void run() {
        enter_menu();
    }
}
