package coms3620.fashion.departments.logistics;

import java.util.Map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class ProductRepository {
    private Map<String, Product> products = new LinkedHashMap<>();
    private String filepath;

    public ProductRepository(String filepath) {
        loadProducts(filepath);
        this.filepath = filepath;
    }

    private void loadProducts(String filepath) {
        File file = new File(filepath);

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                    pw.println("name, sku, size, price, quantity");
                }
            } catch (IOException e) {
                System.out.println("Count not create missing file.");
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                String name = parts[0].trim();
                String sku = parts[1].trim();
                String size = parts[2].trim();
                double price = Double.parseDouble(parts[3].trim());
                int quantity = Integer.parseInt(parts[4].trim());

                Product p = new Product(name, sku, size, price, quantity);
                products.put(sku, p);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load product file: " + filepath, e);
        }
    }

    /* Persist to CSV */
    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filepath, false))) {
            pw.println("name, sku, size, price, quantity");
            for (Product product : products.values()) {
                pw.printf("%s, %s, %s, %.2f, %d%n",
                    product.getName(),
                    product.getSKU(),
                    product.getSize(),
                    product.getPrice(),
                    product.getQuantity());
            }
        } catch (IOException e) {
            System.out.println("Failed to write to products.csv.");
        }
    }

    public boolean reduceProductQuantity(String sku, int quantity) {
        return products.get(sku).reduceQuantity(quantity);
    }

    public void increaseProductQuantity(String sku, int amount) {
        products.get(sku).increaseQuantity(amount);
        save();
    }

    public boolean containsProduct(String name) {
        return products.containsKey(name);
    }

    public Collection<Product> getAll() {
        return products.values();
    }

    public List<Product> getProductByName(String name) {
        return products.values()
               .stream()
               .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
               .toList();

    }
}
