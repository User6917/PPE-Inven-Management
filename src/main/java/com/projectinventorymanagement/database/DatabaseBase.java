package com.projectinventorymanagement.database;

import com.projectinventorymanagement.utils.FileHandler2;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class DatabaseBase {
    public HashMap<Integer, ArrayList<String>> boundList;
    private final String fileName;

    /**
     * Constructor loads the boundList from file via FileHandler.
     * 
     * @param fileName The base name of the file (without extension) where data is
     *                 stored.
     */
    public DatabaseBase(String fileName) {
        this.fileName = fileName;
        // Load boundList from file. FileHansdler.loadFromFile returns a
        // HashMap<Integer, ArrayList<String>>
        this.boundList = FileHandler2.loadFromFile(fileName);
    }

    /**
     * Returns the in-memory data.
     */
    public HashMap<Integer, ArrayList<String>> getEntries() {
        return boundList;
    }

    /**
     * Adds or updates an entry at the specified index.
     * 
     * @param index The row index where the entry should be added.
     * @param entry The ArrayList<String> representing the row data.
     */
    public void addEntry(int index, ArrayList<String> entry) {
        boundList.put(index, entry);
        saveData();
    }

    /**
     * Updates an existing entry at the specified index.
     * 
     * @param index The row index to update.
     * @param entry The new data for that row.
     */
    public void updateEntry(int index, ArrayList<String> entry) {
        if (boundList.containsKey(index)) {
            boundList.put(index, entry);
            saveData();
        }
    }

    /**
     * Deletes an entry from the in-memory data.
     * 
     * @param index The row index to delete.
     */
    public void deleteEntry(int index) {
        boundList.remove(index);
        saveData();
    }

    /**
     * Persists the current boundList data to the file using FileHandler.
     */
    public void saveData() {
        FileHandler2.saveToFile(fileName, boundList);
    }

    public void loadData() {
        this.boundList = FileHandler2.loadFromFile(fileName);
    }

    protected abstract String toStringFormat(ArrayList<String> entry);

    protected abstract ArrayList<String> fromStringFormat(String line);
}