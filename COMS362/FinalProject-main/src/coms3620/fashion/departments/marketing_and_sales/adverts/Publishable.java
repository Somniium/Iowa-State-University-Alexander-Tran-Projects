package coms3620.fashion.departments.marketing_and_sales.adverts;

import java.util.UUID;

import coms3620.fashion.menus.marketing_and_sales.Approvable;

public interface Publishable extends Approvable{

    public String getName();
    public String getType();
    public UUID getId();
    public int getQuarterlyCost();
    public String getMedia();

}
