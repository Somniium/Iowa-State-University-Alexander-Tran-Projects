/**
 * @author Alexander Tran
 */

package coms3620.fashion.menus.productdevelopment;

import java.util.*;
import java.util.stream.Collectors;
import coms3620.fashion.departments.product_development.Prototype;
import coms3620.fashion.departments.product_development.PrototypeRepository;
import coms3620.fashion.menus.Option;
import coms3620.fashion.util.InputValidation;

public class FilterByMaterial implements Option {

    private final PrototypeRepository repo;

    public FilterByMaterial(PrototypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public String getName() {
        return "Filter by Material";
    }

    @Override
    public void run() {
        List<Prototype> all = repo.findAll();
        if (all.isEmpty()) {
            System.out.println("No prototypes to filter.");
            return;
        }

        // distinct materials + counts
        Map<String, Long> matCounts = all.stream()
                .collect(Collectors.groupingBy(Prototype::getMaterials, Collectors.counting()));

        List<String> mats = new ArrayList<>(matCounts.keySet());
        if (mats.size() == 1) {   // only one material – skip picker
            showForMaterial(mats.get(0));
            return;
        }

        System.out.println("Select material:");
        for (int i = 0; i < mats.size(); i++) {
            System.out.printf("%d. %s (%d prototype(s))%n", i + 1, mats.get(i), matCounts.get(mats.get(i)));
        }

        int idx = InputValidation.IntegerRangeInput(1, mats.size()) - 1;
        showForMaterial(mats.get(idx));
    }

    private void showForMaterial(String material) {
        List<Prototype> filtered = repo.findAll().stream()
                .filter(p -> p.getMaterials().equalsIgnoreCase(material))
                .toList();

        if (filtered.isEmpty()) {
            System.out.println("No prototypes use " + material + ".");
            return;
        }

        System.out.printf("Prototypes using %s:%n", material);
        for (Prototype p : filtered) {
            System.out.printf("  %s  (ID: %s)%n", p.getConceptName(), p.getId());
        }
    }
}
