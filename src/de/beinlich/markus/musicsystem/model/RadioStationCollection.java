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
public class RadioStationCollection extends MusicCollection {

    protected static RadioStationCollection uniqueInstance;

    /**
     *
     */
    private RadioStationCollection() {
        super();
        RecordCollectionConnector rcc = new RecordCollectionConnector(dbc);
        this.records = rcc.readRadios();
    }

    ;
         public static synchronized MusicCollection getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new RadioStationCollection();
        }
        return uniqueInstance;
    }
}
