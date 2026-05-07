package coms3620.fashion.menus.productdevelopment;

import coms3620.fashion.departments.product_development.Prototype;
import coms3620.fashion.departments.product_development.PrototypeCostEstimator;
import coms3620.fashion.departments.product_development.PrototypeRepository;
import coms3620.fashion.menus.Option;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EstimatePrototypeCost implements Option {

    private final PrototypeRepository repo;
    private final PrototypeCostEstimator estimator;

    public EstimatePrototypeCost(PrototypeRepository repo,
            PrototypeCostEstimator estimator) {
        this.repo = repo;
        this.estimator = estimator;
    }

    @Override
    public String getName() {
        return "Estimate Prototype Production Cost";
    }

    @Override
    public void run() {
        List<Prototype> prototypes = repo.findAll();
        if (prototypes.isEmpty()) {
            System.out.println("No prototypes available to evaluate.");
            return;
        }

        // Display prototypes
        System.out.println("");
        for (int i = 0; i < prototypes.size(); i++) {
            Prototype p = prototypes.get(i);
            System.out.printf("%d. %s [id=%s, status=%s] materials: %s%n",
                    i + 1,
                    p.getConceptName(),
                    p.getId(),
                    p.isApproved() ? "APPROVED" : "PENDING",
                    p.getMaterials());

        }

        Scanner in = new Scanner(System.in);
        System.out.print("Select a prototype to estimate cost (number): ");
        String rawChoice = in.nextLine().trim();

        int index;
        try {
            index = Integer.parseInt(rawChoice) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }

        if (index < 0 || index >= prototypes.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        Prototype selected = prototypes.get(index);

        System.out.println("\n=== Cost Breakdown for Prototype ===");
        System.out.println("Concept Name : " + selected.getConceptName());
        System.out.println("Materials    : " + selected.getMaterials());
        System.out.println("------------------------------------");

        // Breakdown
        Map<String, Double> breakdown = estimator.breakdown(selected);

        if (breakdown.isEmpty()) {
            System.out.println("Cannot compute cost: materials list is empty.");
            return;
        }

        double total = 0.0;
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            String material = entry.getKey();
            double price = entry.getValue();
            total += price;
            System.out.printf("• %-20s $%.2f%n", material, price);
        }

        System.out.println("------------------------------------");
        System.out.printf("Total Estimated Cost: $%.2f%n", total);
    }
}
