package coms3620.fashion.menus.marketing_and_sales.manage_adverts;

import coms3620.fashion.departments.marketing_and_sales.AdvertManager;
import coms3620.fashion.menus.Option;

public class ViewAdverts implements Option{

    private AdvertManager advertManager;

    public ViewAdverts(AdvertManager advertManager) {
        this.advertManager = advertManager;
    }

    @Override
    public String getName() {
        return "View Adverts";
    }

    @Override
    public void run() {
        advertManager.loadData();
        advertManager.viewAdverts();
    }
    
}
