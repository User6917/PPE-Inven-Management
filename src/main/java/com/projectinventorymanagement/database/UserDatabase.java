package com.projectinventorymanagement.database;

import com.projectinventorymanagement.models.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserDatabase extends DatabaseBase {
    private HashMap<String, User> usersByUsername;
    private HashMap<String, User> usersByID;
    private int highestUserID = 0; // <- Track highest user ID

    public UserDatabase() {
        super("user"); // <- Loads data from "data/user.txt"
        usersByUsername = new HashMap<>();
        usersByID = new HashMap<>();
        loadUsersFromBoundList();
    }

    // Loads users from the boundList loaded by DatabaseBase
    private void loadUsersFromBoundList() {
        if (boundList == null || boundList.isEmpty()) {
            System.out.println("No existing user data found.");
            return;
        }
    
        boolean isFirstRow = true; // <- Track first row (header)
    
        for (Map.Entry<Integer, ArrayList<String>> entry : boundList.entrySet()) {
            ArrayList<String> row = entry.getValue();
    
            // Skip the header row
            if (isFirstRow) {
                isFirstRow = false;
                continue;
            }
    
            if (row.size() == 8) {
                int id = Integer.parseInt(row.get(0));
                // Get the raw value for isActive.
                String rawActive = row.get(7).trim();
                // If rawActive is "-" treat it as active (true); otherwise parse the boolean.
                boolean active = rawActive.equals("-") ? true : Boolean.parseBoolean(rawActive);
                User user = new User(id, row.get(1), row.get(2), row.get(3), row.get(4), row.get(5), row.get(6), active);
                usersByUsername.put(user.getUsername(), user);
                usersByID.put(Integer.toString(user.getUserID()), user);
                highestUserID = Math.max(highestUserID, id);
            }
        }
    }

    public boolean addUser(User user) {
        if (usersByUsername.containsKey(user.getUsername())) {
            return false; // <- User already exists
        }
        user.setUserID(++highestUserID); // Assign new unique user ID
        usersByUsername.put(user.getUsername(), user);
        usersByID.put(Integer.toString(user.getUserID()), user);

        // Create an entry row for the boundList
        ArrayList<String> userRow = new ArrayList<>(Arrays.asList(
            Integer.toString(user.getUserID()),
            user.getName(),
            user.getUsername(),
            user.getPassword(),
            user.getUserType(),
            user.getEmail(),
            user.getPhone(),
            String.valueOf(user.isActive())
        ));
        // Use the next available index as the row key
        int newRowIndex = getEntries().size();
        addEntry(newRowIndex, userRow);
        return true;
    }

    public User authenticate(String username, String password) {
        User user = usersByUsername.get(username);
        if (user != null && user.checkPassword(password) && user.isActive()) {
            return user;
        }
        return null;
    }

    public User getUserByUsername(String username) {
        return usersByUsername.get(username);
    }

    public User getUserByUserID(String id) {
        return usersByID.get(id);
    }

    public boolean updateUser(String username, User updatedUser) {
        if (!usersByUsername.containsKey(username)) {
            return false;
        }
        User existing = usersByUsername.get(username);
        updatedUser.setUserID(existing.getUserID()); // <- Preserve original ID
        usersByUsername.put(username, updatedUser);
        usersByID.put(Integer.toString(updatedUser.getUserID()), updatedUser);

        // Update the user in boundList by iterating over its entries
        for (Map.Entry<Integer, ArrayList<String>> entry : boundList.entrySet()) {
            ArrayList<String> row = entry.getValue();
            if (row.size() > 2 && row.get(2).equals(username)) {
                row.set(1, updatedUser.getName());
                row.set(2, updatedUser.getUsername());
                row.set(3, updatedUser.getPassword());
                row.set(4, updatedUser.getUserType());
                row.set(5, updatedUser.getEmail());
                row.set(6, updatedUser.getPhone());
                row.set(7, String.valueOf(updatedUser.isActive()));
                break;
            }
        }
        saveData();
        return true;
    }

    public boolean deleteUser(String username) {
        if (!usersByUsername.containsKey(username)) {
            return false;
        }
        usersByUsername.remove(username);
        // Remove from boundList: iterate over entries and remove matching username
        boundList.entrySet().removeIf(entry -> entry.getValue().size() > 2 && entry.getValue().get(2).equals(username));
        saveData();
        return true;
    }

    public void displayUsers() {
        for (User user : usersByUsername.values()) {
            user.display();
        }
    }

    // Implementation of abstract methods for serialization/deserialization.
    @Override
    protected String toStringFormat(ArrayList<String> entry) {
        return String.join(",", entry);
    }

    @Override
    protected ArrayList<String> fromStringFormat(String line) {
        return new ArrayList<>(Arrays.asList(line.split(",")));
    }
}