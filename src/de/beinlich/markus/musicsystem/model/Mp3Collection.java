/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.model.db.RecordCollectionConnector;

/**
 *
 * @author Markus Beinlich
 */
class Mp3Collection extends MusicCollection {
    protected static Mp3Collection uniqueInstance;
 
    /**
     *
     */
    private Mp3Collection () {
        super();
        RecordCollectionConnector rcc = new RecordCollectionConnector();
        this.records = rcc.readRecords("CD");
     };
    
    public static synchronized MusicCollection getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Mp3Collection();
        }
        return uniqueInstance;
    }
}
