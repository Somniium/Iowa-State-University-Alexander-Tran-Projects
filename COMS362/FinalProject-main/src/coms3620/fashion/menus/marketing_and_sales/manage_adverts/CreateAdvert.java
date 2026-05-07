package coms3620.fashion.menus.marketing_and_sales.manage_adverts;

import coms3620.fashion.menus.Option;
import coms3620.fashion.departments.marketing_and_sales.AdvertManager;

public class CreateAdvert implements Option {

    private AdvertManager advertManager;

    public CreateAdvert(AdvertManager advertManager) {
        this.advertManager = advertManager;
    }

    @Override
    public String getName() {
        return "Create Advert";
    }

    @Override
    public void run() {
        advertManager.loadData();
        advertManager.createAdvert();
        advertManager.saveData();
    }
    
}
