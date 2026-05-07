package coms3620.fashion.menus.productdevelopment;

import coms3620.fashion.departments.product_development.Prototype;
import coms3620.fashion.departments.product_development.PrototypeRepository;
import coms3620.fashion.menus.Option;
import java.util.List;
import java.util.Scanner;

public class UpdatePrototypeMaterials implements Option {

    private final PrototypeRepository repo;

    public UpdatePrototypeMaterials(PrototypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public String getName() {
        return "Update Prototype Materials";
    }

    @Override
    public void run() {
        List<Prototype> prototypes = repo.findAll();
        if (prototypes.isEmpty()) {
            System.out.println("No prototypes exist yet.");
            return;
        }

        System.out.println("");
        for (int i = 0; i < prototypes.size(); i++) {
            Prototype p = prototypes.get(i);
            System.out.printf("%d. %s [id=%s, status=%s]%n",
                    i + 1,
                    p.getConceptName(),
                    p.getId(),
                    p.isApproved() ? "APPROVED" : "PENDING");
        }

        Scanner in = new Scanner(System.in);
        System.out.print("Select a prototype to update (number): ");
        String rawChoice = in.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(rawChoice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection. Please enter a number from the list.");
            return;
        }

        if (index < 0 || index >= prototypes.size()) {
            System.out.println("Invalid selection. Please enter a number from the list.");
            return;
        }

        Prototype oldProto = prototypes.get(index);

        if (oldProto.isApproved()) {
            System.out.println("Approved prototypes cannot be modified.");
            return;
        }

        System.out.println("Current materials: " + oldProto.getMaterials());
        System.out.print("Enter new materials list: ");
        String newMaterials = in.nextLine().trim();

        if (newMaterials.isEmpty()) {
            System.out.println("Materials cannot be empty. Update cancelled.");
            return;
        }

        // Create a NEW Prototype with updated materials (since there is no setMaterials)
        Prototype updated = new Prototype(
                oldProto.getId(),
                oldProto.getConceptName(),
                newMaterials,
                oldProto.isApproved()
        );

        // Replace old with new using your repository API
        boolean removed = repo.delete(oldProto);
        if (!removed) {
            System.out.println("Could not remove old prototype from repository. No changes saved.");
            return;
        }

        // add() also calls save(), so this persists to CSV
        repo.add(updated);

        System.out.println("Materials updated successfully.");
    }
}
