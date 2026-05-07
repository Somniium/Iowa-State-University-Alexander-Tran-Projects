package coms3620.fashion.departments.product_development;

public class ProductDesigner {
    private final String name;

    public ProductDesigner(String name) {
        this.name = name;
    }

    public void requestPrototype(PrototypeController controller, String concept, String materials) {
        controller.createPrototype(concept, materials, this.name);
    }
}
