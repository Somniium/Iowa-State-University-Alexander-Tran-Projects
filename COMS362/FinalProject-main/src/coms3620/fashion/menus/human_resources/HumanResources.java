package coms3620.fashion.menus.human_resources;

import coms3620.fashion.departments.human_resources.HRMain;
import coms3620.fashion.menus.Option;

public class HumanResources implements Option {

    @Override
    public String getName() {
        return "Human Resources";
    }

    @Override
    public void run() {
        HRMain.runHR();  // Call your existing HR system
    }
}
