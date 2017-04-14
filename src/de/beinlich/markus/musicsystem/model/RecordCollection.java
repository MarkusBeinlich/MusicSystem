/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.model.db.RecordCollectionConnector;
import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
class RecordCollection extends MusicCollection {
    protected static RecordCollection uniqueInstance;

    private RecordCollection () {
        super();
        RecordCollectionConnector rcc = new RecordCollectionConnector(dbc);
        this.records = rcc.readRecords("Record");
     };
     
    public static synchronized MusicCollection getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new RecordCollection();
        }
        return uniqueInstance;
    }
    
}
