package com.projectinventorymanagement.database;

import java.util.ArrayList;
import java.util.Arrays;

public class HospitalDatabase extends DatabaseBase {

    public HospitalDatabase() {
        super("hospital"); // Loads data from "data/hospital.txt"
    }

    /**
     * Adds a new hospital entry to the database.
     * Computes totalItemsReceived from the individual item counts.
     */
    public void addHospital(String hospitalCode, String hospitalName, int HC, int FS, int MS, int GL, int GW, int SC, boolean isActive) {
        // Calculate total items received.
        int totalItems = HC + FS + MS + GL + GW + SC;
        ArrayList<String> entry = new ArrayList<>(Arrays.asList(
            hospitalCode,
            hospitalName,
            Integer.toString(totalItems),
            Integer.toString(HC),
            Integer.toString(FS),
            Integer.toString(MS),
            Integer.toString(GL),
            Integer.toString(GW),
            Integer.toString(SC),
            Boolean.toString(isActive)
        ));
        // Use the next available row index.
        int newRow = getEntries().size();
        addEntry(newRow, entry);
    }
    

    /**
     * Finds a hospital by its code.
     */
    public ArrayList<String> findHospitalByCode(String hospitalCode) {
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty() && row.get(0).equals(hospitalCode)) {
                return row;
            }
        }
        return null;
    }

    /**
     * Displays all hospital entries (for debugging).
     */
    public void displayHospitals() {
        for (int i = 0; i < getEntries().size(); i++) {
            ArrayList<String> row = getEntries().get(i);
            System.out.println(String.join(", ", row));
        }
    }

    // Retrieve Hospital Names
    public ArrayList<String> getNames() {
        ArrayList<String> hospitalNames = new ArrayList<>();
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty()) {
                hospitalNames.add(row.get(1));
            }
        }
        return hospitalNames;
    }

    // Retrieves Hospital Code via Hospital Name
    public String getCodeByName(String hospitalName) {
        for (ArrayList<String> row : getEntries().values()) {
            if (!row.isEmpty() && row.get(1).equals(hospitalName)) {
                return row.get(0);
            }
        }
        return null;
    }

    @Override
    protected String toStringFormat(ArrayList<String> entry) {
        return String.join(",", entry);
    }

    @Override
    protected ArrayList<String> fromStringFormat(String line) {
        return new ArrayList<>(Arrays.asList(line.split(",")));
    }
}