package coms3620.fashion.departments.product_development;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class FileStorage implements Storage {
    private final Path path;

    public FileStorage(String filePath) {
        this.path = Paths.get(filePath);
        try {
            if (!Files.exists(path)) Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize storage: " + e.getMessage());
        }
    }

    @Override
    public void write(String line) throws IOException {
        Files.write(path, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    }

    @Override
    public List<String> readAll() throws IOException {
        return Files.readAllLines(path);
    }
}
