package coms3620.fashion.departments.product_development;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PrototypeCostEstimator {

    private final Map<String, Double> costByMaterial = new HashMap<>();
    private final double defaultCostPerMaterial;

    public PrototypeCostEstimator(String csvPath, double defaultCostPerMaterial) {
        this.defaultCostPerMaterial = defaultCostPerMaterial;
        loadCosts(csvPath);
    }

    private void loadCosts(String csvPath) {
        File f = new File(csvPath);
        if (!f.exists() || f.length() == 0) {
            System.out.println("Cost estimator: no material cost CSV found at \""
                    + csvPath + "\" – using defaults only.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                // skip header line
                if (first) {
                    first = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    continue; // malformed line
                }

                String materialName = parts[0].trim().toLowerCase();
                String priceStr = parts[1].trim();

                try {
                    double price = Double.parseDouble(priceStr);
                    costByMaterial.put(materialName, price);
                } catch (NumberFormatException e) {
                    System.out.println("Cost estimator: invalid price \""
                            + priceStr + "\" for material \"" + materialName + "\" – skipping.");
                }
            }
        } catch (IOException e) {
            System.out.println("Cost estimator: failed to read material cost CSV: " + e.getMessage());
        }
    }

    public Map<String, Double> breakdown(Prototype prototype) {
        Map<String, Double> result = new HashMap<>();

        String materials = prototype.getMaterials();
        if (materials == null || materials.isBlank()) {
            return result;
        }

        String[] tokens = materials.split(",");
        for (String token : tokens) {
            String normalized = token.trim().toLowerCase();
            if (normalized.isEmpty()) {
                continue;
            }

            double price = costByMaterial.getOrDefault(normalized, defaultCostPerMaterial);
            result.put(normalized, price);
        }
        return result;
    }

    public double estimateCost(Prototype prototype) {
        String materials = prototype.getMaterials();
        if (materials == null || materials.isBlank()) {
            // Cannot calculate meaningful cost without materials
            return 0.0;
        }

        String[] tokens = materials.split(",");
        double total = 0.0;

        for (String token : tokens) {
            String normalized = token.trim().toLowerCase();
            if (normalized.isEmpty()) {
                continue;
            }

            double price = costByMaterial.getOrDefault(normalized, defaultCostPerMaterial);
            total += price;
        }

        return total;
    }
}
