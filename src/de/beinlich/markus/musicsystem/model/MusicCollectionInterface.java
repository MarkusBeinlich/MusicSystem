/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public interface MusicCollectionInterface {

    Record getRecord();

    List<Record> getRecords();

    void addRecord(Record record);

    Record getRecord(int i);

    void registerObserver(MusicCollectionObserver o);

}
