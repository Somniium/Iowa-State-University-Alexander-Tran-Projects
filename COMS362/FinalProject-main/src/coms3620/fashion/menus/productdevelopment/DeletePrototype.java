package coms3620.fashion.menus.productdevelopment;

import java.util.List;
import coms3620.fashion.departments.product_development.Prototype;
import coms3620.fashion.departments.product_development.PrototypeRepository;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.InputValidation;

public class DeletePrototype implements Option {

    private final PrototypeRepository repo;

    public DeletePrototype(PrototypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public String getName() {
        return "Delete Prototype";
    }

    @Override
    public void run() {
        List<Prototype> all = repo.findAll();
        if (all.isEmpty()) {
            System.out.println("No prototypes to delete.");
            return;
        }

        for (int i = 0; i < all.size(); i++) {
            Prototype p = all.get(i);
            System.out.printf("%d. %s (materials: %s)  ID: %s%n",
                    i + 1, p.getConceptName(), p.getMaterials(), p.getId());
        }

        int idx = InputValidation.IntegerRangeInput(1, all.size()) - 1;
        Prototype chosen = all.get(idx);

        System.out.printf(
                "Delete prototype \"%s\" (ID: %s)?%n1=Yes  0=Cancel%n",
                chosen.getConceptName(), chosen.getId()
        );

        if (InputValidation.IntegerRangeInput(0, 1) == 1) {
            boolean removed = repo.delete(chosen);
            if (removed) {
                System.out.println("Prototype " + chosen.getId() + " deleted.");
            } else {
                System.out.println("ERROR: Could not delete prototype (maybe already removed?).");
            }
        }
    }
}
