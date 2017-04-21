package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.model.db.DatabaseConnection;
import java.util.*;

class MusicCollection implements MusicCollectionInterface {

    protected transient static DatabaseConnection dbc = new DatabaseConnection();
    protected List<Record> records;

    protected MusicCollection() {
    }

    public static synchronized MusicCollection getInstance(String format) {
        switch (format) {
            case "CdPlayer":
                return CdCollection.getInstance();
            case "Mp3Player":
                return Mp3Collection.getInstance();
            case "RecordPlayer":
                return RecordCollection.getInstance();
            case "Radio":
                return RadioStationCollection.getInstance();
            default:
                throw new NoSuchElementException("Unbekanntes Format:" + format);
        }
    }
    
      public MusicCollectionDto getMusicCollectionDto() {
        MusicCollectionDto musicCollectionDto = new MusicCollectionDto();
        musicCollectionDto.records = new ArrayList<>();
        for (Record record : records) {
            musicCollectionDto.records.add(record.getDto());
        }
        return musicCollectionDto;
    }

    @Override
    public RecordInterface getRecord() {
        return records.get(0);
    }

    @Override
    public RecordInterface getRecordById(int rid) {
        for(Record record: records){
            if (record.getRid() == rid){
                return record;
            }
        }
        return records.get(0);
    }

    @Override
    public void addRecord(Record record) {
        records.add(record);
    }


    @Override
    public RecordInterface getRecord(int i) {
        return records.get(i);
    }

    @Override
    public List<RecordInterface> getRecords() {
        return Collections.unmodifiableList(records);
    }

    @Override
    public void registerObserver(MusicCollectionObserver o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
