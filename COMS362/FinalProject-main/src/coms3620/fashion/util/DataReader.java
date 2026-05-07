package coms3620.fashion.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class DataReader implements AutoCloseable {

    private BufferedReader File;

    public DataReader(String filepath) throws FileNotFoundException {
        File = new BufferedReader(new FileReader(filepath));
    }

    /**
     * Reads in one line from the csv, uses the first entry from that line to
     * define the object types of all other elements The line is then parsed
     * using DataReader.getRow()
     *
     * @author Joshua Morningstar
     * @throws Exception
     */
    public Object[] getEncodedRow() throws Exception {
        String line = File.readLine();
        if (line == null) {
            return null;
        }
        String[] full_elements = parseLine(line);
        String elementTypes = full_elements[0];
        String[] elements = Arrays.copyOfRange(full_elements, 1, full_elements.length);
        return parseElements(elements, elementTypes);
    }

    /**
     * Reads in one line from the csv, returns an array of objects whose type is determine by the elements string, returns null at eof
     * elementTypes string can contain
     * i - integer
     * u - UUID
     * d - LocalDate
     * b - boolean
     * s - string
     * For example, iss would expect the csv to contain an integer followed by two strings
     * 
     * @author Joshua Morningstar
     * @throws Exception
     */
    public Object[] getRow(String elementTypes) throws Exception {
        String line = File.readLine();
        if (line == null) {
            return null;
        }
        String[] elements = parseLine(line);
        return parseElements(elements, elementTypes);
    }

    private Object[] parseElements(String[] elements, String elementTypes) throws Exception {
        if (elements.length != elementTypes.length()) {
            throw new LengthMismatchException("Number of elements doesn't match length of elementTypes");
        }
        Object[] objects = new Object[elements.length];
        for (int i = 0; i < elementTypes.length(); i++) {
            switch (elementTypes.charAt(i)) {
                case 'i':
                    objects[i] = Integer.parseInt(elements[i]);
                    break;
                case 'u':
                    objects[i] = UUID.fromString(elements[i]);
                    break;
                case 'd':
                    objects[i] = LocalDate.parse(elements[i], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    break;
                case 'b':
                    objects[i] = Boolean.parseBoolean(elements[i].trim());
                    break;
                case 's':
                    objects[i] = elements[i];
                    break;
                default:
                    throw new Exception("Invalid elementType: " + elementTypes.charAt(i));
            }
        }
        return objects;
    }

    private String[] parseLine(String line) {
        char[] charArray = line.toCharArray();
        List<String> strings = new ArrayList<String>();
        String string = "";
        boolean in_quotes = false;
        boolean start_of_entry = true;
        for (int i = 0; i < charArray.length; i++) {
            if (start_of_entry) {
                if (charArray[i] == '"') {
                    in_quotes = true;
                    i++;
                }
                start_of_entry = false;
            }
            if ((charArray[i] == ',') && !in_quotes) {
                strings.add(string);
                string = "";
                start_of_entry = true;
            } else if (charArray[i] == '"' && in_quotes) {
                in_quotes = false;
            } else {
                string += charArray[i];
            }
        }
        strings.add(string);
        return strings.toArray(new String[0]);
    }

    public void close() throws IOException {
        File.close();
    }

    public class LengthMismatchException extends IOException {

        public LengthMismatchException(String message) {
            super(message);
        }

        public LengthMismatchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
