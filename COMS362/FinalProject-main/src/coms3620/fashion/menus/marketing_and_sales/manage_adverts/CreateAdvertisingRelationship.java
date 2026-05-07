package coms3620.fashion.menus.marketing_and_sales.manage_adverts;

import coms3620.fashion.departments.marketing_and_sales.AdvertManager;
import coms3620.fashion.menus.Option;

public class CreateAdvertisingRelationship implements Option {

    private AdvertManager advertManager;

    public CreateAdvertisingRelationship(AdvertManager advertManager) {
        this.advertManager = advertManager;
    }

    @Override
    public String getName() {
        return "Create advertising relationship.";
    }

    @Override
    public void run() {
        advertManager.loadData();
        advertManager.createAdvertisingRelationship();
        advertManager.saveData();
    }
    
}
