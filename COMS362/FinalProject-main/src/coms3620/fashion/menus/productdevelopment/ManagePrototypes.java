package coms3620.fashion.menus.productdevelopment;

import coms3620.fashion.departments.product_development.*;
import coms3620.fashion.menus.Menu;
import coms3620.fashion.menus.Option;

public class ManagePrototypes extends Menu implements Option {

    public ManagePrototypes() {
        // 1. create the repository and controller once
        PrototypeRepository repo = new PrototypeRepository("data/product_development/prototypes.csv");
        PrototypeController controller = new PrototypeController(new MaterialManager(), repo);
        PrototypeCostEstimator estimator = new PrototypeCostEstimator("data/product_development/material_costs.csv", 25.0);

        // 2. pass them to the menu options that need them
        addOption(new CreatePrototype(controller));
        addOption(new ViewPrototypes(repo));
        addOption(new ApproveRejectPrototype(repo));
        addOption(new FilterByMaterial(repo));
        addOption(new DeletePrototype(repo));
        addOption(new DesignContest(repo));
        addOption(new UpdatePrototypeMaterials(repo));
        addOption(new EstimatePrototypeCost(repo, estimator));
    }

    @Override
    public String getName() {
        return "Manage Prototypes";
    }

    @Override
    public void run() {
        enter_menu();
    }
}
