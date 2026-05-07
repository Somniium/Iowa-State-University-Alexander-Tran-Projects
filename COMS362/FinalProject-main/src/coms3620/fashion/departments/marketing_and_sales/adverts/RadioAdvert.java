package coms3620.fashion.departments.marketing_and_sales.adverts;

import java.util.UUID;

import coms3620.fashion.util.InputValidation;
import coms3620.fashion.util.Stdin;

public class RadioAdvert implements Advert{ 

    private String name;
    private UUID id;
    private int quarterlyCost;
    private String mediaPath;
    private String approvalStatus = "pending";

    private static String type = "Radio Advert";

    @Override
    public String getMedia() {
        return mediaPath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public int getQuarterlyCost() {
        return quarterlyCost;
    }

    @Override
    public void createFromInput() {
        System.out.println("Enter advertisement name: ");
        name = Stdin.nextLine();
        System.out.println("Enter quarterly cost of advertisment: ");
        quarterlyCost = InputValidation.IntegerMinInput(0);
        System.out.println("Enter path to media files: ");
        mediaPath = Stdin.nextLine();
        id = UUID.randomUUID();
    }

    @Override
    public void loadFromFile(Object[] object) {
        name = (String)object[1];
        quarterlyCost = (int)object[2];
        mediaPath = (String)object[3];
        id = (UUID)object[4];
    }

    @Override
    public Object[] getRowData() {
        return new Object[]{"ssisu", getType(), getName(), getQuarterlyCost(), mediaPath, getId()};
    }

    @Override
    public String getApprovalStatus() {
        return approvalStatus;
    }

    @Override
    public void setApprovalStatus(String status) {
        approvalStatus = status;
    }
    
}
