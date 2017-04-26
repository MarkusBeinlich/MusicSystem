/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.guifx;

import de.beinlich.markus.musicsystem.model.MusicCollectionInterface;
import de.beinlich.markus.musicsystem.model.Record;
import de.beinlich.markus.musicsystem.model.RecordInterface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Markus
 */
public class RecordsModel {

        private final ObservableList<RecordInterface> records;

        public RecordsModel(MusicCollectionInterface musicCollection) {
            records = FXCollections.observableArrayList(musicCollection.getRecords());
        }
        
        public ObservableList<RecordInterface> getRecords() {
            return records;
        }
    }

