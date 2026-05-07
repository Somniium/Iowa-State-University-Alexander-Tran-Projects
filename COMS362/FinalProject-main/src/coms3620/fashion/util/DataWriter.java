package coms3620.fashion.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DataWriter implements AutoCloseable {

    private BufferedWriter File;

    public DataWriter(String path) throws IOException {
        File = new BufferedWriter(new FileWriter(path));
    }

    /**
     * Writes a row of objects by calling their toString method
     *
     * @author Joshua Morningstar
     * @throws IOException
     */
    public void putRow(Object... elements) throws IOException {
        for (int i = 0; i < elements.length; i++) {
            File.append(elements[i].toString());
            if (i != elements.length - 1) {
                File.append(",");
            }
        }
        File.append("\n");
    }

    public void close() throws IOException {
        File.close();
    }
}
