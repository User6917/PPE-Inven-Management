package com.projectinventorymanagement.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FileHandler2 {
    private static final String DATA_PATH = "data/";

    // Ensures the file exists (creates it if necessary)
    private static void fileExist(String filename) {
        String fileNameWithExtension = filename + ".txt";
        try {
            File file = new File(DATA_PATH + fileNameWithExtension);
            file.getParentFile().mkdirs();
            if (file.createNewFile()) {
                System.out.println("File " + file.getName() + " created at " + file.getPath());
            }
        } catch (IOException e) {
            System.err.println("Error creating file: " + fileNameWithExtension);
            e.printStackTrace();
        }
    }

    /**
     * Saves the given data (boundList) to a file.
     * @param filename The base file name (without extension).
     * @param data The HashMap representing rows of data.
     */
    public static void saveToFile(String filename, HashMap<Integer, ArrayList<String>> data) {
        fileExist(filename);
        String fileNameWithExtension = filename + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_PATH + fileNameWithExtension))) {
            for (int i = 0; i < data.size(); i++) {
                ArrayList<String> row = data.get(i);
                if (row != null) {
                    writer.write(String.join(",", row));
                    writer.newLine();
                }
            }
            System.out.println("File saved successfully: " + fileNameWithExtension);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + fileNameWithExtension);
            e.printStackTrace();
        }
    }

    /**
     * Loads data from a file and returns it as a HashMap.
     * Each line in the file corresponds to a row and is split by commas.
     * @param filename The base file name (without extension).
     * @return A HashMap where the key is the row index and the value is an ArrayList of String values.
     */
    public static HashMap<Integer, ArrayList<String>> loadFromFile(String filename) {
        fileExist(filename);
        String fileNameWithExtension = filename + ".txt";
        HashMap<Integer, ArrayList<String>> data = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_PATH + fileNameWithExtension))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                // Split each line by commas and store as an ArrayList
                ArrayList<String> rowData = new ArrayList<>(Arrays.asList(line.split(",")));
                data.put(row, rowData);
                row++;
            }
            System.out.println("File loaded successfully: " + fileNameWithExtension);
        } catch (IOException e) {
            System.err.println("Error reading file: " + fileNameWithExtension);
            e.printStackTrace();
        }
        return data;
    }
}
