package coms3620.fashion.menus.legal;

import coms3620.fashion.departments.legal.LegalManager;
import coms3620.fashion.menus.Option;

public class ProcessAdvertisingRelationship implements Option {
    
    private LegalManager legalManager;

    public ProcessAdvertisingRelationship(LegalManager legalManager) {
        this.legalManager = legalManager;
    }

    @Override
    public String getName() {
        return "Process Advertising Relationship";
    }

    @Override
    public void run() {
        legalManager.loadData();
        legalManager.approveAdvertisingRelationship();
        legalManager.saveData();
    }

}
