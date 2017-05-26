package de.beinlich.markus.musicsystem.model.db;

import java.util.*;

/**
 *
 * @author IBB Teilnehmer
 */
public class RecordTester {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        new RecordTester().go();
    }

    private void go() {
        RecordConnector rc = new RecordConnector();
        
        List<RecordDto> records = rc.readRecords();
        for (RecordDto record : records) {
            System.out.println(record);
        }
        
        rc.close();
    }
    
}
