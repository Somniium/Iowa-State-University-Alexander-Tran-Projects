package coms3620.fashion.menus.marketing_and_sales.manage_adverts;

import coms3620.fashion.departments.marketing_and_sales.AdvertManager;
import coms3620.fashion.menus.Option;

public class ApproveAdvert implements Option{
    
    private AdvertManager advertManager;

    public ApproveAdvert(AdvertManager advertManager) {
        this.advertManager = advertManager;
    }

    @Override
    public String getName() {
        return "Approve or Deny Adverts";
    }

    @Override
    public void run() {
        advertManager.loadData();
        advertManager.approveAdverts();
        advertManager.saveData();
    }
}
