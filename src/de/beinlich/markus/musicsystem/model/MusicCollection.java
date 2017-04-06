/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.model.db.DatabaseConnection;
import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Markus
 */
public class MusicCollection implements MusicCollectionInterface, Serializable{

    protected transient static DatabaseConnection dbc = new DatabaseConnection();
    protected List<Record> records;
     
    protected MusicCollection() {
    }

//    public static synchronized MusicCollection getInstance() {
//        if (uniqueInstance == null) {
//            uniqueInstance = new MusicCollection();
//        }
//        return uniqueInstance;
//    }
    
    public Record getRecord(){
        return records.get(0);
    }
     
    /**
     *
     * @param record
     */
    public void addRecord (Record record){
         records.add(record);
     }
     
    /**
     *
     * @param i
     * @return
     */
    public Record getRecord(int i) {
         return records.get(i);
     }
     
    /**
     *
     * @return
     */
    public List<Record> getRecords() {
         return Collections.unmodifiableList(records);
     }

    @Override
    public void registerObserver(MusicCollectionObserver o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
