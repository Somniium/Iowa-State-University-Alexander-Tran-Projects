package coms3620.fashion.departments.marketing_and_sales;

import coms3620.fashion.departments.marketing_and_sales.adverts.*;
import coms3620.fashion.util.InputValidation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.Objects;


public class AdvertFactory {

    private Map<String,Supplier<Advert>> advertTypeMap = new HashMap<String,Supplier<Advert>>();

    public AdvertFactory(){
        //Define constructors for user input
        advertTypeMap.put("Magazine Advert", ()->new MagazineAdvert());
        advertTypeMap.put("Radio Advert", ()->new RadioAdvert());
        advertTypeMap.put("TV Advert", ()->new TVAdvert());
    }

    public <T extends Advert> void addOption(Class<T> advert, String displayName, String type){
        //TODO
    }

    public Advert createAdvertFromInput() {
        System.out.println("What type of advertisement are you creating?");
        Supplier<Advert> advertConstructor = selectOption(advertTypeMap);
        if(Objects.isNull(advertConstructor)) {return null;}
        Advert advert = advertConstructor.get();
        advert.createFromInput();
        return advert;
    }

    public Advert createAdvertFromObject(Object[] object) {
        Supplier<Advert> advertConstructor = advertTypeMap.get(object[0]);
        if(Objects.isNull(advertConstructor)) {return null;}
        Advert advert = advertConstructor.get();
        advert.loadFromFile(object);
        return advert;
    }

    private <T> T selectOption(Map<String,T> options) {
        String[] labels = new String[options.size()];
        int i = 0;
        System.out.println("0: Cancel");
        for(String label: options.keySet()) {
            System.out.println((i+1) +": " + label);
            labels[i] = label;
            i++;
        }
        int userInput = InputValidation.IntegerRangeInput(0, options.size());
        if(userInput == 0) {
            return null;
        }
        else {
            return options.get(labels[userInput-1]);
        }
    }

    
}
