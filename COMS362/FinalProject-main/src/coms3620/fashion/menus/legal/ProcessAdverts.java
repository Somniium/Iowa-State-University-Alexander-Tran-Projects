package coms3620.fashion.menus.legal;

import coms3620.fashion.departments.legal.LegalManager;
import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;

public class ProcessAdverts extends Menu implements Option {

    private LegalManager legalManager;

    public ProcessAdverts(LegalManager legalManager) {
        this.legalManager = legalManager;
    }

    @Override
    public String getName() {
        return "Process Adverts";
    }

    @Override
    public void run() {
        legalManager.loadData();
        legalManager.approveAdvert();
        legalManager.saveData();
    }
    
}
