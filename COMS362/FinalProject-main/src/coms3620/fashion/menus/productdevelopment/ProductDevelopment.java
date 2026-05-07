package coms3620.fashion.menus.productdevelopment;

import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;

public class ProductDevelopment extends Menu implements Option {

    public ProductDevelopment() {
        ManagePrototypes managePrototypes = new ManagePrototypes();
        addOption(managePrototypes);
    }

    @Override
    public String getName() {
        return "Product Development";
    }

    @Override
    public void run() {
        enter_menu();
    }
}
