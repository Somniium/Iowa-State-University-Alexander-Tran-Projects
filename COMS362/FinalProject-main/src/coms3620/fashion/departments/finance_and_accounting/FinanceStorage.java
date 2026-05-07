package coms3620.fashion.departments.finance_and_accounting;

import coms3620.fashion.departments.product_development.Storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Stores and retrieves plain text lines from a single file */
public class FinanceStorage implements Storage {

    private final Path filePath;

    /**
     * Creates a FinanceStorage that reads/writes to the given file path.
     */
    public FinanceStorage(String fileName) {
        this.filePath = Paths.get(fileName);
    }

    /**
     * Appends a single line to the underlying file.
     */
    @Override
    public void write(String line) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write(line);
            writer.newLine();
        }
    }

    /**
     * Reads all lines from the underlying file.
     * Returns an empty list if the file does not exist.
     */
    @Override
    public List<String> readAll() throws IOException {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(filePath);
    }
}
