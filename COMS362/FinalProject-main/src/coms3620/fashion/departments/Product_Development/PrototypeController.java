package coms3620.fashion.departments.product_development;

public class PrototypeController {

    private final MaterialManager materialManager;
    private final PrototypeRepository repo;

    public PrototypeController(MaterialManager mm, PrototypeRepository repo) {
        this.materialManager = mm;
        this.repo = repo;
    }

    public void createPrototype(String concept, String materials, String requestedBy) {
        if (!materialManager.available(materials)) {
            System.out.println("ERROR: Materials unavailable. Canceling request.");
            return;
        }
        Prototype p = new Prototype(concept, materials);
        repo.add(p);
        System.out.println("OK: Prototype created and saved. UUID = " + p.getId());
    }
}
