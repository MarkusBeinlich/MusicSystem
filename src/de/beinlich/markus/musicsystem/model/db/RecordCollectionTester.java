package de.beinlich.markus.musicsystem.model.db;

import de.beinlich.markus.musicsystem.model.PlayListComponentInterface;
import de.beinlich.markus.musicsystem.model.*;
import java.util.*;

/**
 *
 * @author IBB Teilnehmer
 */
public class RecordCollectionTester {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        new RecordCollectionTester().go();
    }

    private void go() {
            DatabaseConnection dbc = new DatabaseConnection();
        RecordCollectionConnector rc = new RecordCollectionConnector(dbc);

        List<de.beinlich.markus.musicsystem.model.Record> records = rc.readRecords("CD");
        for (de.beinlich.markus.musicsystem.model.Record record : records) {
            System.out.println(record);
            for (PlayListComponentInterface track : record.getTracks()) {
                System.out.println(track);
            }
        }
    }

}
