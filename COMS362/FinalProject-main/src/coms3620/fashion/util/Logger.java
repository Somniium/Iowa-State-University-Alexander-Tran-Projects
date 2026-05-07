package coms3620.fashion.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger implements AutoCloseable {

    private BufferedWriter File;

    public Logger(String path) throws IOException {
        File = new BufferedWriter(new FileWriter(path));
    }

    public void putRow(String log) throws IOException {
        File.append(LocalDateTime.now() + " - " + log + "\n");
    }


    @Override
    public void close() throws Exception {
        File.close();
    }

}
