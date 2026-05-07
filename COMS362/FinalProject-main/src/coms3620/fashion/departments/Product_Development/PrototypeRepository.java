package coms3620.fashion.departments.product_development;

import coms3620.fashion.util.DataReader;
import coms3620.fashion.util.DataWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PrototypeRepository {

    private final String filePath;
    private final List<Prototype> cache = new ArrayList<>();

    public PrototypeRepository(String filePath) {
        this.filePath = filePath;
        File f = new File(filePath);
        if (f.exists() && f.length() > 0) {
            load();
        } else {
            System.out.println("Repository: no existing CSV (“" + filePath + "”) – starting empty.");
        }
    }

    /*  load from CSV  */
    private void load() {
        try (DataReader dr = new DataReader(filePath)) {
            dr.getRow("ssssss");        // skip header
            Object[] row;
            while ((row = dr.getRow("ussbss")) != null) {
                UUID id = (UUID) row[0];
                String concept = (String) row[1];
                String materials = (String) row[2];
                boolean approved = (Boolean) row[3];
                String actor = row.length > 4 ? (String) row[4] : "";
                String note = row.length > 5 ? (String) row[5] : "";
                Prototype p = new Prototype(id, concept, materials, approved);
                p.setLastActor(actor);
                p.setLastNote(note);
                cache.add(p);
            }
        } catch (Exception e) {
            System.out.println("No existing prototype file found—starting fresh.");
        }
    }

    /*  persist to CSV  */
    public void save() {
        try (DataWriter dw = new DataWriter(filePath)) {
            dw.putRow("id", "concept", "materials", "approved", "lastActor", "lastNote");
            for (Prototype p : cache) {
                dw.putRow(p.toRow());
            }
        } catch (IOException e) {
            System.out.println("Failed to save prototypes: " + e.getMessage());
        }
    }

    public void add(Prototype p) {
        cache.add(p);
        save();
    }

    public List<Prototype> findAll() {
        return Collections.unmodifiableList(cache);
    }

    public boolean delete(Prototype prototype) {
        boolean removed = cache.remove(prototype);
        if (removed) {
            save();   // persist updated list to CSV
        }
        return removed;
    }
}
