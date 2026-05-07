package coms3620.fashion.menus.marketing_and_sales.manage_adverts;
import coms3620.fashion.departments.marketing_and_sales.AdvertManager;
import coms3620.fashion.menus.Option;

public class ViewAdvertisingRelationships implements Option {

    private AdvertManager advertManager;

    public ViewAdvertisingRelationships(AdvertManager advertManager) {
        this.advertManager = advertManager;
    }

    @Override
    public String getName() {
        return "View Advertising Relationships";
    }

    @Override
    public void run() {
        advertManager.loadData();
        advertManager.viewAdvertisingRelationships();
    }
    
}
