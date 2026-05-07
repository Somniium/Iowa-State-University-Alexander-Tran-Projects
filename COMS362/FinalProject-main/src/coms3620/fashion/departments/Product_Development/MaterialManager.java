package coms3620.fashion.departments.product_development;

public class MaterialManager {
    // Rudimentary material availability check
    public boolean available(String materials) {
        return materials != null && !materials.trim().isEmpty();
    }
}
