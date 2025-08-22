package com.projectinventorymanagement.utils;

import java.util.HashMap;
import java.util.ArrayList;

public interface DataSource {
    HashMap<Integer, ArrayList<String>> readData(String source);
    void writeData(String source, HashMap<Integer, ArrayList<String>> data);
}
