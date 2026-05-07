package coms3620.fashion;

import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.marketing_and_sales.MarketingAndSales;
import coms3620.fashion.menus.productdevelopment.ProductDevelopment;
import coms3620.fashion.menus.logistics.Logistics;
import coms3620.fashion.menus.human_resources.HumanResources;
import coms3620.fashion.menus.legal.Legal;
import coms3620.fashion.menus.finance_and_accounting.Finance;

public class FashionManager extends Menu {

    public FashionManager() {
        // Define options
        MarketingAndSales marketing_and_sales = new MarketingAndSales();
        Logistics logistics = new Logistics();
        HumanResources human_resources = new HumanResources();
        ProductDevelopment product_development = new ProductDevelopment();
        Finance finance = new Finance();
        // Add options
        addOption(marketing_and_sales);
        addOption(logistics);
        addOption(human_resources);
        addOption(product_development);
        addOption(new Legal());
        addOption(finance);
    }
}
