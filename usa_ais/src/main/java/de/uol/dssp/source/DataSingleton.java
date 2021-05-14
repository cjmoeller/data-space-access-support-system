package de.uol.dssp.source;


import tech.tablesaw.api.Table;

import java.io.IOException;

/**
 * Reads and provides CSV-data.
 */
public class DataSingleton {
    private static DataSingleton instance = null;
    private final Table table;

    public static DataSingleton getInstance() {
        if(instance == null)
            instance = new DataSingleton();
        return instance;
    }


    public DataSingleton(){
        Table aisDataRaw = null;
        try {
            aisDataRaw = Table.read().csv("src/main/resources/AIS_2020_01_28_0.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.table = aisDataRaw;

    }

    public Table getTable() {
        return table;
    }
}
