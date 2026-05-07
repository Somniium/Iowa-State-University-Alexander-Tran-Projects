package coms3620.fashion.menus.marketing_and_sales.manage_adverts;

import coms3620.fashion.departments.marketing_and_sales.AdvertManager;
import coms3620.fashion.menus.Option;

public class CancelPublishedAdvert implements Option {

    private AdvertManager advertManager;

    public CancelPublishedAdvert(AdvertManager advertManager) {
        this.advertManager = advertManager;
    }

    @Override
    public String getName() {
        return "Cancel Published Advertisement";
    }

    @Override
    public void run() {
        advertManager.loadData();
        advertManager.cancelPublishedAdvert();
        advertManager.saveData();
    }
    
}
