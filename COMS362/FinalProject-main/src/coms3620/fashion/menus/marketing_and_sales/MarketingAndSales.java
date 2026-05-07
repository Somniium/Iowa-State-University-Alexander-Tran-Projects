package coms3620.fashion.menus.marketing_and_sales;

import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;
import coms3620.fashion.menus.marketing_and_sales.manage_adverts.ManageAdverts;

public class MarketingAndSales extends Menu implements Option {

    public MarketingAndSales() {
        // Define options
        ViewBudget view_budget = new ViewBudget();
        ManageAdverts manageAdvert = new ManageAdverts();
        // Add options
        addOption(view_budget);
        addOption(manageAdvert);
    }

    @Override
    public String getName() {
        return "Marketing and Sales";
    }

    @Override
    public void run() {
        enter_menu();
    }
}
