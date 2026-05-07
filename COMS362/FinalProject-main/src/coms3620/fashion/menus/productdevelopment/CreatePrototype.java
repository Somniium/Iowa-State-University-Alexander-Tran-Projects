package coms3620.fashion.menus.productdevelopment;

import coms3620.fashion.departments.product_development.PrototypeController;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.Stdin;

public class CreatePrototype implements Option {

    private final PrototypeController controller;

    public CreatePrototype(PrototypeController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "Create Prototype";
    }

    @Override
    public void run() {
        System.out.print("Enter concept name: ");
        String concept = Stdin.nextLine();

        System.out.print("Enter materials: ");
        String materials = Stdin.nextLine();

        controller.createPrototype(concept, materials, "Designer");
    }
}
