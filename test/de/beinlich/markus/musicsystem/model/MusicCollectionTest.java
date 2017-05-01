/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.lib.MusicCollectionObserver;
import de.beinlich.markus.musicsystem.lib.RecordInterface;
import de.beinlich.markus.musicsystem.lib.MusicCollectionDto;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Markus
 */
public class MusicCollectionTest implements MusicCollectionObserver {

    private MusicCollection musicCollection;
    private int updateMusicCollection = 1;
    private Record record1;
    private Record record2;
    private Record record3;

    public MusicCollectionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
//        musicCollection = MusicCollection.getInstance("CdPlayer");
        musicCollection = new MusicCollection();
        record1 = new Record(1, "title1", "artist1", null);
        record2 = new Record(2, "title2", "artist2", null);
        record3 = new Record(3, "title3", "artist3", null);
        Track track1 = new Track("titletrack1", 100, 1, "fileName1");
        Track track2 = new Track("titletrack2", 200, 2, "fileName2");
        record1.addTrack(track1);
        record1.addTrack(track2);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class MusicCollection.
     */
    @Test
    public void testGetInstanceRadio() {
        System.out.println("getInstance");
        String format = "Radio";
        MusicCollection expResult = null;
        MusicCollection result = MusicCollection.getInstance(format);
        assertTrue(result instanceof RadioStationCollection);
    }

    @Test
    public void testGetInstanceCdPlayer() {
        System.out.println("getInstance");
        String format = "CdPlayer";
        MusicCollection expResult = null;
        MusicCollection result = MusicCollection.getInstance(format);
        assertTrue(result instanceof CdCollection);
    }

    @Test
    public void testGetInstanceMp3Player() {
        System.out.println("getInstance");
        String format = "Mp3Player";
        MusicCollection expResult = null;
        MusicCollection result = MusicCollection.getInstance(format);
        assertTrue(result instanceof Mp3Collection);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetInstanceException() {
        System.out.println("getInstance");
        String format = "????";
        MusicCollection expResult = null;
        MusicCollection result = MusicCollection.getInstance(format);
        assertTrue(result instanceof CdCollection);
    }

    /**
     * Test of getMusicCollectionDto method, of class MusicCollection.
     */
    @Test
    public void testGetMusicCollectionDto() {
        System.out.println("getMusicCollectionDto");
        MusicCollection instance = new MusicCollection();
        instance.addRecord(record1);
        instance.addRecord(record2);
        MusicCollectionDto result = instance.getMusicCollectionDto();
        assertEquals("titletrack1", result.records.get(0).getTracks().get(0).getTitle());
    }

    /**
     * Test of getRecord method, of class MusicCollection.
     */
    @Test
    public void testGetRecord_0args() {
        System.out.println("getRecord");
        MusicCollection instance = new MusicCollection();
        RecordInterface expResult = record1;
        instance.addRecord(record1);
        RecordInterface result = instance.getRecord();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRecordById method, of class MusicCollection.
     */
    @Test
    public void testGetRecordById() {
        System.out.println("getRecordById");
        int rid = 0;
        MusicCollection instance = new MusicCollection();
        RecordInterface expResult = record1;
        instance.addRecord(record1);
        instance.addRecord(record2);
        RecordInterface result = instance.getRecordById(1);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRecordByIdNotExisting() {
        System.out.println("getRecordById");
        MusicCollection instance = new MusicCollection();
        RecordInterface expResult = record1;
        instance.addRecord(record1);
        instance.addRecord(record2);
        RecordInterface result = instance.getRecordById(9999);
        assertEquals(expResult, result);
    }

    /**
     * Test of addRecord method, of class MusicCollection.
     */
    @Test
    public void testAddRecord() {
        System.out.println("addRecord");
        MusicCollection instance = new MusicCollection();
        instance.addRecord(record1);
        instance.addRecord(record1);
        instance.addRecord(record2);
        assertEquals(3, instance.records.size());
    }

    /**
     * Test of getRecord method, of class MusicCollection.
     */
    @Test
    public void testGetRecord_int() {
        System.out.println("getRecord");
        int i = 0;
        MusicCollection instance = new MusicCollection();
        RecordInterface expResult = record1;
        instance.addRecord(record1);
        instance.addRecord(record2);
        RecordInterface result = instance.getRecord(0);
        assertEquals(expResult, result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRecord_intNitExisting() {
        System.out.println("getRecord");
        int i = 0;
        MusicCollection instance = new MusicCollection();
        RecordInterface expResult = record1;
        instance.addRecord(record1);
        instance.addRecord(record2);
        RecordInterface result = instance.getRecord(9999);
        assertEquals(expResult, result);
    }

    /**
     * Test of getRecords method, of class MusicCollection.
     */
    @Test
    public void testGetRecords() {
        System.out.println("getRecords");
        MusicCollection instance = new MusicCollection();
        instance.addRecord(record1);
        instance.addRecord(record2);
        List<RecordInterface> result = instance.getRecords();
        assertEquals(2, result.size());
    }

    /**
     * Test of notifyMusicCollectionObservers method, of class MusicCollection.
     */
    @Test
    public void testNotifyMusicCollectionObservers() {
        System.out.println("notifyMusicCollectionObservers");
        MusicCollection instance = new MusicCollection();
        instance.registerObserver(this);
        int anzMusicCollectionObserverCalls = updateMusicCollection;
        instance.notifyMusicCollectionObservers();
        assertEquals(anzMusicCollectionObserverCalls + 1, updateMusicCollection);
    }

    /**
     * Test of registerObserver method, of class MusicCollection.
     */
    @Test
    public void testRegisterObserver() {
        System.out.println("registerObserver");
        MusicCollection instance = new MusicCollection();
        instance.registerObserver(this);
        int anzMusicCollectionObserverCalls = updateMusicCollection;
        instance.notifyMusicCollectionObservers();
        assertEquals(anzMusicCollectionObserverCalls + 1, updateMusicCollection);
    }

    /**
     * Test of setFormat method, of class MusicCollection.
     */
    @Test
    public void testSetFormat() {
        System.out.println("setFormat");

        MusicCollection instance = new MusicCollection();
        instance.registerObserver(this);
        int anzMusicCollectionObserverCalls = updateMusicCollection;
        instance.setFormat("Radio");
        assertEquals(anzMusicCollectionObserverCalls + 1, updateMusicCollection);
    }

    @Override
    public void updateMusicCollection() {
        updateMusicCollection++;
    }

}
