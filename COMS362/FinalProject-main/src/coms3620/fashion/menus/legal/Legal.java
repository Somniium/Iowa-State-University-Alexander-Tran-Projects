package coms3620.fashion.menus.legal;

import coms3620.fashion.departments.legal.LegalManager;
import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;

public class Legal extends Menu implements Option {

    private LegalManager legalManager = new LegalManager();

    public Legal() {
        addOption(new ProcessAdverts(legalManager));
        addOption(new ProcessAdvertisingRelationship(legalManager));
    }

    @Override
    public String getName() {
        return "Legal";
    }

    @Override
    public void run() {
        enter_menu();
    }
    
}
