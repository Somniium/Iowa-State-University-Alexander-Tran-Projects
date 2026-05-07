package coms3620.fashion.menus.marketing_and_sales.manage_adverts;

import coms3620.fashion.departments.marketing_and_sales.AdvertManager;
import coms3620.fashion.menus.Option;

public class ApproveAdvertisingRelationship implements Option{

    AdvertManager advertManager;

    public ApproveAdvertisingRelationship(AdvertManager advertManager) {
        this.advertManager = advertManager;
    }

    @Override
    public String getName() {
        return "Approve or Deny advertising relationships.";
    }

    @Override
    public void run() {
        advertManager.loadData();
        advertManager.approveAdvertisingRelationship();
        advertManager.saveData();
    }
    
}
