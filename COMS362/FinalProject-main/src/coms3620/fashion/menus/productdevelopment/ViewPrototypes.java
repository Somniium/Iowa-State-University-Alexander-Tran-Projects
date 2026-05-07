package coms3620.fashion.menus.productdevelopment;

import coms3620.fashion.departments.product_development.PrototypeRepository;
import coms3620.fashion.menus.Option;

import java.util.List;

public class ViewPrototypes implements Option {

    private final PrototypeRepository repo;

    public ViewPrototypes(PrototypeRepository repo) {
        this.repo = repo;
    }

    @Override
    public String getName() {
        return "View Prototypes";
    }

    @Override
    public void run() {
        List<?> protos = repo.findAll();   // or List<Prototype>
        if (protos.isEmpty()) {
            System.out.println("No prototypes found.");
        } else {
            protos.forEach(System.out::println);
        }
    }
}