package coms3620.fashion.departments.marketing_and_sales.adverts;

public interface Advert extends Publishable {

    public void createFromInput();
    public void loadFromFile(Object[] object);
    public default Object[] getRowData() {
        return new Object[]{"ssiu", getType(), getName(), getQuarterlyCost(), getId()};
    };

}
