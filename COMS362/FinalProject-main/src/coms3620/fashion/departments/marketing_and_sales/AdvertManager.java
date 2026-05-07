package coms3620.fashion.departments.marketing_and_sales;
import coms3620.fashion.departments.marketing_and_sales.adverts.Advert;
import coms3620.fashion.departments.marketing_and_sales.adverts.AdvertisingRelationship;
import coms3620.fashion.departments.marketing_and_sales.adverts.PublishedAdvert;
import coms3620.fashion.util.DataWriter;
import coms3620.fashion.util.InputValidation;
import coms3620.fashion.util.DataReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class AdvertManager {

    private List<Advert> adverts = new ArrayList<Advert>();
    private List<PublishedAdvert> publishedAdverts = new ArrayList<PublishedAdvert>();
    private List<AdvertisingRelationship> advertisingRelationships = new ArrayList<AdvertisingRelationship>();
    private AdvertFactory advertFactory = new AdvertFactory();

    public void loadData() {
        try {
            adverts = new ArrayList<Advert>();
            DataReader reader = new DataReader("data/marketing_and_sales/adverts.csv");
            Object[] object;
            while((object = reader.getEncodedRow()) != null) {
                Advert advert = advertFactory.createAdvertFromObject(object);
                if(Objects.isNull(advert)) {System.out.println("Error reading in advertisment"); continue;}
                adverts.add(advert);
            }
            reader.close();

            publishedAdverts = new ArrayList<PublishedAdvert>();
            reader = new DataReader("data/marketing_and_sales/publishedAdverts.csv");
            while((object = reader.getEncodedRow()) != null) {
                PublishedAdvert publishedAdvert = new PublishedAdvert(object);
                publishedAdverts.add(publishedAdvert);
            }
            reader.close();

            advertisingRelationships = new ArrayList<AdvertisingRelationship>();
            reader = new DataReader("data/marketing_and_sales/advertisingRelationships.csv");
            while((object = reader.getEncodedRow()) != null) {
                AdvertisingRelationship advertisingRelationship = new AdvertisingRelationship(object);
                advertisingRelationships.add(advertisingRelationship);
            }
            reader.close();
        }
        catch(FileNotFoundException e) {}
        catch (Exception e) {
            System.out.println("Failed to read data");
            System.out.println(e);
        }
    }

    public void saveData() {
        try {
            DataWriter writer = new DataWriter("data/marketing_and_sales/adverts.csv");
            for(Advert advert : adverts) {
                writer.putRow(advert.getRowData());
            }
            writer.close();

            writer = new DataWriter("data/marketing_and_sales/publishedAdverts.csv");
            for(PublishedAdvert publishedAdvert : publishedAdverts) {
                writer.putRow(publishedAdvert.getRowData());
            }
            writer.close();

            writer = new DataWriter("data/marketing_and_sales/advertisingRelationships.csv");
            for(AdvertisingRelationship advertisingRelationship : advertisingRelationships) {
                writer.putRow(advertisingRelationship.getRowData());
            }
            writer.close();

        } catch (IOException e) {
            System.out.println("Failed to save data");
            System.out.println(e);
        }
        
    }

    public void createAdvert() {
        Advert advert = advertFactory.createAdvertFromInput();
        if(Objects.isNull(advert)) {
            System.out.println("Advert creation cancelled");
        }
        else {
            adverts.add(advert);
        }
    }

    public void publishAdvert() {
        if(advertisingRelationships.size() == 0) {
            System.out.println("Error, no advertising relationships to use for publishing.");
            return;
        }
        String[] advertNames = new String[adverts.size()];
        for(int i = 0; i < adverts.size(); i++) {
            advertNames[i] = adverts.get(i).getName();
        }
        String[] companyNames = new String[advertisingRelationships.size()];
        for(int i = 0; i < advertisingRelationships.size(); i++) {
            companyNames[i] = advertisingRelationships.get(i).getName();
        }
        Advert advertToPublish = adverts.get(InputValidation.OptionsInput(advertNames));
        System.out.println("What company will be running the advert?");
        String companyName = companyNames[InputValidation.OptionsInput(companyNames)];
        PublishedAdvert publishedAdvert = new PublishedAdvert(advertToPublish.getName(), advertToPublish.getType(), advertToPublish.getQuarterlyCost(), advertToPublish.getId(), 1, companyName);
        publishedAdverts.add(publishedAdvert);
    }

    public void approveAdverts() {
        String[] advertNames = new String[publishedAdverts.size()];
        for(int i = 0; i < publishedAdverts.size(); i++) {
            advertNames[i] = publishedAdverts.get(i).getName();
        }
        PublishedAdvert advertToApprove = publishedAdverts.get(InputValidation.OptionsInput(advertNames));
        advertToApprove.run();
    }

    public void addAdvert(Advert advert) {
        adverts.add(advert);
    }

    public void viewAdverts() {
        System.out.println("name, quarterlyCost, type, id");
        for(Advert advert : adverts) {
            System.out.println(advert.getName() + ", " + advert.getQuarterlyCost() + ", " + advert.getType() + ", " + advert.getId());
        }
    }

    public void deleteAdvert() {
    
        String[] advertNames = new String[adverts.size()];
        for(int i = 0; i < adverts.size(); i++) {
        advertNames[i] = adverts.get(i).getName();
        } 
        Advert advertToDelete = adverts.get(InputValidation.OptionsInput(advertNames));
        UUID advertId = advertToDelete.getId();
        ArrayList<PublishedAdvert> associatedPublishedAdverts = new ArrayList<>();
        for(PublishedAdvert publishedAdvert : publishedAdverts) {
            if(publishedAdvert.getId().equals(advertId)) {
                associatedPublishedAdverts.add(publishedAdvert);
            } 
       }
        if(associatedPublishedAdverts.size() > 0) {
            System.out.println("This advertisement has associated published adverts. Are you sure you want to delete it, and cancel all associated published adverts?");
            int choice = InputValidation.OptionsInput(new String[]{"No","Yes"});
            if(choice == 1) {
                for(PublishedAdvert publishedAdvert : associatedPublishedAdverts) {
                    publishedAdverts.remove(publishedAdvert);
                }
            }
            else {
                System.out.println("Deletion cancelled");
                return;
            }
       }
       adverts.remove(advertToDelete);
    }

    public void viewPublishedAdverts() {
        System.out.println("type, name, quarterlyCost, id, startDate, endDate, approvalStatus, advertCompany");
        for(PublishedAdvert advert : publishedAdverts) {
            Object[] data = advert.getRowData();
            String string = "";
            for(Object object : data) {
                string += (object.toString() + ", ");
            }
            System.out.println(string);
        }
    }

    public void cancelPublishedAdvert() {
    String[] advertNames = new String[publishedAdverts.size()];
        for(int i = 0; i < adverts.size(); i++) {
            advertNames[i] = publishedAdverts.get(i).getName();
        }
        publishedAdverts.remove(InputValidation.OptionsInput(advertNames));
    }

    public void createAdvertisingRelationship() {
        AdvertisingRelationship advertisingRelationship = new AdvertisingRelationship();
        advertisingRelationships.add(advertisingRelationship);
    }

    public void viewAdvertisingRelationships() {
        System.out.println("companyName, approvalStatus contractFile");
        for(AdvertisingRelationship advertisingRelationship : advertisingRelationships) {
            System.out.println(advertisingRelationship.getName() + ", " + advertisingRelationship.getApprovalStatus() + ", " + advertisingRelationship.getContractFile());
        }
    }

    public void approveAdvertisingRelationship() {
        String[] advertNames = new String[advertisingRelationships.size()];
        for(int i = 0; i < advertisingRelationships.size(); i++) {
            advertNames[i] = advertisingRelationships.get(i).getName();
        }
        AdvertisingRelationship advertToApprove = advertisingRelationships.get(InputValidation.OptionsInput(advertNames));
        advertToApprove.run();
    }

    public void cancelAdvertisingRelationships() {
        // TODO
    }

    public List<AdvertisingRelationship> getApprovedAdvertisers() {
        List<AdvertisingRelationship> approvedAdvertisers = new ArrayList<AdvertisingRelationship>();
        for(AdvertisingRelationship advertisingRelationship : advertisingRelationships) {
            if(advertisingRelationship.getApprovalStatus() == "approved") {
                approvedAdvertisers.add(advertisingRelationship);
            }
        }
        return approvedAdvertisers;
    }

}

