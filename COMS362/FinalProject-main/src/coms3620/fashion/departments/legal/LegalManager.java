package coms3620.fashion.departments.legal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coms3620.fashion.departments.marketing_and_sales.adverts.AdvertisingRelationship;
import coms3620.fashion.departments.marketing_and_sales.adverts.PublishedAdvert;
import coms3620.fashion.util.DataReader;
import coms3620.fashion.util.DataWriter;
import coms3620.fashion.util.InputValidation;

public class LegalManager {

    private List<PublishedAdvert> publishedAdverts = new ArrayList<PublishedAdvert>();
    private List<AdvertisingRelationship> advertisingRelationships = new ArrayList<AdvertisingRelationship>();
    

    public void loadData() {
        try {

            Object[] object;
            publishedAdverts = new ArrayList<PublishedAdvert>();
            DataReader reader = new DataReader("data/marketing_and_sales/publishedAdverts.csv");
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
            DataWriter writer = new DataWriter("data/marketing_and_sales/publishedAdverts.csv");
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

    public List<PublishedAdvert> getPendingAdverts() {
        List<PublishedAdvert> pendingAdverts = new ArrayList<>();
        for(PublishedAdvert advert : publishedAdverts) {
            if(advert.getApprovalStatus().equals("waiting for legal")) {
                pendingAdverts.add(advert);
            }
        }
        return pendingAdverts;
    }

    public List<AdvertisingRelationship> getPendingAdvertisingRelationships() {
        List<AdvertisingRelationship> pendingAdvertisingRelationships = new ArrayList<>();
        for(AdvertisingRelationship relationship : advertisingRelationships) {
            if(relationship.getApprovalStatus().equals("waiting for legal")) {
                pendingAdvertisingRelationships.add(relationship);
            }
        }
        return pendingAdvertisingRelationships;
    }

    public void approveAdvert() {
        List<PublishedAdvert> pendingAdverts = getPendingAdverts();
        if(pendingAdverts.size() == 0) {
            System.out.println("Error, no adverts to approve.");
            return;
        }
        String[] advertNames = new String[pendingAdverts.size()];
        for(int i = 0; i < pendingAdverts.size(); i++) {
            advertNames[i] = pendingAdverts.get(i).getName();
        }
        String[] companyNames = new String[advertisingRelationships.size()];
        for(int i = 0; i < advertisingRelationships.size(); i++) {
            companyNames[i] = advertisingRelationships.get(i).getName();
        }
        PublishedAdvert advertToPublish = pendingAdverts.get(InputValidation.OptionsInput(advertNames));
        advertToPublish.draftLegal();
    }

    public void approveAdvertisingRelationship() {
        List<AdvertisingRelationship> pendingRelationships = getPendingAdvertisingRelationships();
        if(pendingRelationships.size() == 0) {
            System.out.println("Error, no advertising relationships to approve.");
            return;
        }
        String[] advertNames = new String[pendingRelationships.size()];
        for(int i = 0; i < pendingRelationships.size(); i++) {
            advertNames[i] = pendingRelationships.get(i).getName();
        }
        String[] companyNames = new String[advertisingRelationships.size()];
        for(int i = 0; i < advertisingRelationships.size(); i++) {
            companyNames[i] = advertisingRelationships.get(i).getName();
        }
        AdvertisingRelationship relationshipToApprove = pendingRelationships.get(InputValidation.OptionsInput(advertNames));
        relationshipToApprove.draftLegal();
    }
}