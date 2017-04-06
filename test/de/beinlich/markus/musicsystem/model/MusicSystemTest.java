/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import java.util.*;
import java.util.logging.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Markus Beinlich
 */
public class MusicSystemTest implements VolumeObserver, TrackTimeObserver, TrackObserver, StateObserver, RecordObserver, MusicPlayerObserver {

    private MusicSystem musicSystem;
    private Record testRecord;
    private int updateVolume = 0;
    private int updateState = 0;
    private int updateTrackTime = 0;
    private int updateTrack = 0;
    private int updateRecord = 0;
    private int updateMusicPlayer = 0;

    public MusicSystemTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        testRecord = new Record("testTitle", "testArtist");
        testRecord.addTrack(new Track("testTrack1", 11, 1, ""));
        testRecord.addTrack(new Track("testTrack2", 12, 2, ""));
        testRecord.addTrack(new Track("testTrack3", 13, 3, ""));
        musicSystem = MusicSystem.getInstance(null);
        musicSystem.setRecord(testRecord);
        musicSystem.registerObserver((VolumeObserver) this);
        musicSystem.registerObserver((TrackTimeObserver) this);
        musicSystem.registerObserver((TrackObserver) this);
        musicSystem.registerObserver((StateObserver) this);
        musicSystem.registerObserver((RecordObserver) this);
        musicSystem.registerObserver((MusicPlayerObserver) this);
    }

    @After
    public void tearDown() {
        musicSystem.removeObserver((VolumeObserver) this);
        musicSystem.removeObserver((TrackTimeObserver) this);
        musicSystem.removeObserver((TrackObserver) this);
        musicSystem.removeObserver((StateObserver) this);
        musicSystem.removeObserver((RecordObserver) this);
        musicSystem.removeObserver((MusicPlayerObserver) this);
    }

    /**
     * Test of getInstance method, of class MusicSystem.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        String expResult = "Wohnzimmer";
        MusicSystem result = MusicSystem.getInstance(null);
        assertEquals(expResult, result.getLocation());
    }

    /**
     * Test of readConfiguration method, of class MusicSystem.
     */
    @Test
    public void testReadConfiguration() {
        System.out.println("readConfiguration");
        MusicSystem instance = musicSystem;
//        instance.readConfiguration(null);
        assertEquals("Wohnzimmer", instance.getLocation());
    }

    /**
     * Test of setVolume method, of class MusicSystem.
     */
    @Test
    public void testSetVolume() {
        System.out.println("setVolume");
        double volume = 10;
        double volumeGet;
        MusicSystem instance = musicSystem;
        instance.setVolume(volume);
        volumeGet = instance.getVolume();
        assertEquals(volumeGet, volume, 0.001);
        instance.setVolume(-5);
        volumeGet = instance.getVolume();
        assertEquals(volumeGet, 0, 0.001);
        instance.setVolume(1100);
        volumeGet = instance.getVolume();
        assertEquals(volumeGet, 100, 0.001);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of play method, of class MusicSystem.
     */
    @Test
    public void testPlay() {
        System.out.println("play123");
        MusicSystem instance = musicSystem;
        instance.setMusicSystemState(MusicSystemState.STOP);
        instance.play();
        //Prüfen, ob die Zeit nach 1 Sekunden auf 1 steht
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MusicSystemTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        int t = instance.getCurrentTimeTrack();
        assertEquals(t, 1);

        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of pause method, of class MusicSystem.
     */
    @Test
    public void testPause() {
        System.out.println("pause");
        MusicSystem instance = musicSystem;
        instance.pause();
        assertEquals(MusicSystemState.PAUSE, instance.getMusicSystemState());
    }

    /**
     * Test of stop method, of class MusicSystem.
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        MusicSystem instance = musicSystem;
        instance.stop();
        assertEquals(MusicSystemState.STOP, instance.getMusicSystemState());
    }

    @Test
    public void testStop2() {
        System.out.println("stop2");
        MusicSystem instance = musicSystem;
        instance.next();
        instance.stop();
        assertEquals(instance.getRecord().getTracks().get(0), instance.getCurrentTrack());
    }

    /**
     * Test of previous method, of class MusicSystem.
     */
    @Test
    public void testPrevious() {
        System.out.println("previous");
        MusicSystem instance = musicSystem;
        instance.previous();
        assertEquals(instance.getRecord().getTracks().get(0), instance.getCurrentTrack());
    }

    @Test
    public void testPrevious2() {
        System.out.println("previous");
        MusicSystem instance = musicSystem;
        PlayListComponent oldTrack = instance.getCurrentTrack();
        instance.next();
        instance.previous();
        assertEquals(oldTrack, instance.getCurrentTrack());
    }

    @Test
    public void testPrevious3() {
        System.out.println("previous");
        MusicSystem instance = musicSystem;
        PlayListComponent oldTrack = instance.getCurrentTrack();
        instance.next();
        instance.next();
        instance.previous();
        assertNotEquals(oldTrack, instance.getCurrentTrack());
    }

    /**
     * Test of next method, of class MusicSystem.
     */
    @Test
    public void testNext() {
        System.out.println("next");
        MusicSystem instance = musicSystem;
        PlayListComponent oldTrack = instance.getCurrentTrack();
        instance.next();
        assertNotEquals(oldTrack, instance.getCurrentTrack());
    }

    @Test
    public void testNext2() {
        System.out.println("next");
        MusicSystem instance = musicSystem;
        instance.next();
        assertEquals(testRecord.getTracks().get(1), instance.getCurrentTrack());
    }

    @Test
    public void testNext3() {
        System.out.println("next");
        MusicSystem instance = musicSystem;
        instance.next();
        instance.next();
        instance.next();
        //testRecord hat nur 3 Tracks. Deswegen sollte der currentTrack auch 
        //nach dem 3ten next auf den ersten track stehen
        assertEquals(testRecord.getTracks().get(0), instance.getCurrentTrack());
    }

    /**
     * Test of addSource method, of class MusicSystem.
     */
//    @Test
//    public void testAddSource() {
//        System.out.println("addSource");
//        MusicPlayer source = hifi.getActiveSource();
//        MusicSystem instance = hifi;
//        int anzSources = instance.getSources().size();
//        instance.addSource(source);
//        assertEquals(anzSources,    instance.getSources().size());
//    }
//    
//    @Test
//    public void testAddSource2() {
//        System.out.println("addSource");
//        MusicSystem instance = hifi;
//        int anzSources = instance.getSources().size();
//        instance.addSource(new CdPlayer());
//        assertEquals(anzSources+1,    instance.getSources().size());
//    }
    /**
     * Test of toString method, of class MusicSystem.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        MusicSystem instance = musicSystem;
        String expResult = "Wohnzimmer";
        String result = instance.toString();
        assertTrue(result.contains("Wohnzimmer"));
    }

    /**
     * Test of getCurrentTimeTrack method, of class MusicSystem.
     */
    @Test
    public void testGetCurrentTimeTrack() {
        System.out.println("getCurrentTimeTrack");
        MusicSystem instance = musicSystem;
        musicSystem.play();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(MusicSystemTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        int expResult = 1;
        int result = instance.getCurrentTimeTrack();
        assertEquals(expResult, result);

    }

    /**
     * Test of setCurrentTrack method, of class MusicSystem.
     */
    @Test
    public void testSetCurrentTrack() {
        PlayListComponent track = musicSystem.getRecord().getTracks().get(0);
        MusicSystem instance = musicSystem;
        int anzTrackObserverCalls = updateTrack;
        instance.setCurrentTrack(track);
        assertEquals(anzTrackObserverCalls + 1, updateTrack);
    }

    @Test
    public void testSetCurrentTrack1() {
        PlayListComponent track = musicSystem.getRecord().getTracks().get(0);
        MusicSystem instance = musicSystem;
        instance.setCurrentTrack(track);
        assertEquals(track, instance.getCurrentTrack());
    }

    /**
     * Test of setRecord method, of class MusicSystem.
     */
    @Test
    public void testSetRecord() {
        System.out.println("setRecord");
        Record record = musicSystem.getRecord();
        MusicSystem instance = musicSystem;
        instance.setRecord(record);
        assertEquals(record, instance.getRecord());
    }

    @Test
    public void testSetRecord1() {
        Record record = musicSystem.getRecord();
        MusicSystem instance = musicSystem;
        int anzRecordObserverCalls = updateRecord;
        instance.setRecord(record);
        assertEquals(anzRecordObserverCalls + 1, updateRecord);
    }

    /**
     * Test of registerObserver method, of class MusicSystem.
     */
    @Test
    public void testRegisterObserver_TrackObserver() {
        System.out.println("registerObserver");
        TrackObserver o = (TrackObserver) this;
        MusicSystem instance = musicSystem;
        instance.registerObserver(o);
        int anzTrackObserverCalls = updateTrack;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyTrackObservers();
        assertEquals(anzTrackObserverCalls + 1, updateTrack);
    }

    /**
     * Test of removeObserver method, of class MusicSystem.
     */
    @Test
    public void testRemoveObserver_TrackObserver() {
        System.out.println("removeObserver");
        TrackObserver o = (TrackObserver) this;
        MusicSystem instance = musicSystem;
        instance.removeObserver(o);
        int anzTrackObserverCalls = updateTrack;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyTrackObservers();
        assertEquals(anzTrackObserverCalls, updateTrack);
    }

    /**
     * Test of registerObserver method, of class MusicSystem.
     */
    @Test
    public void testRegisterObserver_RecordObserver() {
        System.out.println("registerObserver");
        RecordObserver o = (RecordObserver) this;
        MusicSystem instance = musicSystem;
        instance.registerObserver(o);
        int anzRecordObserverCalls = updateRecord;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyRecordObservers();
        assertEquals(anzRecordObserverCalls + 1, updateRecord);
    }

    @Test
    public void testRegisterObserver_VolumeObserver() {
        VolumeObserver o = (VolumeObserver) this;
        MusicSystem instance = musicSystem;
        instance.registerObserver(o);
        int anzVolumeObserverCalls = updateRecord;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyVolumeObservers();
        assertEquals(anzVolumeObserverCalls + 1, updateVolume);
    }

    @Test
    public void testRemoveObserver_VolumeObserver() {
        VolumeObserver o = (VolumeObserver) this;
        MusicSystem instance = musicSystem;
        instance.removeObserver(o);
        int anzVolumeObserverCalls = updateRecord;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyVolumeObservers();
        assertEquals(anzVolumeObserverCalls, updateVolume);
    }

    @Test
    public void testRegisterObserver_StateObserver() {
        StateObserver o = (StateObserver) this;
        MusicSystem instance = musicSystem;
        instance.registerObserver(o);
        int anzStateObserverCalls = updateState;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyStateObservers();
        assertEquals(anzStateObserverCalls + 1, updateState);
    }

    @Test
    public void testRemoveObserver_StateObserver() {
        StateObserver o = (StateObserver) this;
        MusicSystem instance = musicSystem;
        instance.removeObserver(o);
        int anzStateObserverCalls = updateState;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyStateObservers();
        assertEquals(anzStateObserverCalls, updateState);
    }

    /**
     * Test of removeObserver method, of class MusicSystem.
     */
    @Test
    public void testRemoveObserver_RecordObserver() {
        System.out.println("removeObserver");
        RecordObserver o = (RecordObserver) this;
        MusicSystem instance = musicSystem;
        instance.removeObserver(o);
        int anzRecordObserverCalls = updateRecord;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyRecordObservers();
        assertEquals(anzRecordObserverCalls, updateRecord);
    }

    /**
     * Test of notifyMusicPlayerObservers method, of class MusicSystem.
     */
    @Test
    public void testNotifyMusicPlayerObservers() {
        System.out.println("notifyMusicPlayerObservers");
        MusicSystem instance = musicSystem;
        int anzMusicPlayerObserverCalls = updateMusicPlayer;
        instance.notifyMusicPlayerObservers();
        assertEquals(anzMusicPlayerObserverCalls + 1, updateMusicPlayer);
    }

    /**
     * Sicherstellen, daß der Observer nicht 2 mal installiert werden kann
     */
    @Test
    public void testRegisterObserver_MusicPlayerObserver() {
        System.out.println("registerObserver");
        MusicPlayerObserver o = (MusicPlayerObserver) this;
        MusicSystem instance = musicSystem;
        instance.registerObserver(o);
        int anzMusicPlayerObserverCalls = updateMusicPlayer;
        instance.notifyMusicPlayerObservers();
        assertEquals(anzMusicPlayerObserverCalls + 1, updateMusicPlayer);
    }

    /**
     * Test of removeObserver method, of class MusicSystem.
     */
    @Test
    public void testRemoveObserver_MusicPlayerObserver() {
        System.out.println("removeObserver");
        MusicPlayerObserver o = (MusicPlayerObserver) this;
        MusicSystem instance = musicSystem;
        instance.removeObserver(o);
        int anzMusicPlayerObserverCalls = updateMusicPlayer;
        instance.notifyMusicPlayerObservers();
        assertEquals(anzMusicPlayerObserverCalls, updateMusicPlayer);
    }

    /**
     * Test of getSources method, of class MusicSystem.
     */
    @Test
    public void testGetSources() {
        System.out.println("getSources");
        MusicSystem instance = musicSystem;
        int anzSources = instance.getSources().size();
        LinkedList<MusicPlayer> result = instance.getSources();
        result.remove();
        assertEquals(anzSources, instance.getSources().size());
    }

    /**
     * Test of hasPlay method, of class MusicSystem.
     */
    @Test
    public void testHasPlay() {
        System.out.println("hasPlay");
        MusicSystem instance = musicSystem;
        boolean expResult = true;
        boolean result = instance.hasPlay();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasStop method, of class MusicSystem.
     */
    @Test
    public void testHasStop() {
        System.out.println("hasStop");
        MusicSystem instance = musicSystem;
        boolean expResult = true;
        boolean result = instance.hasStop();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasNext method, of class MusicSystem.
     */
    @Test
    public void testHasNext() {
        System.out.println("hasNext");
        MusicSystem instance = musicSystem;
        boolean expResult = true;
        boolean result = instance.hasNext();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasPrevious method, of class MusicSystem.
     */
    @Test
    public void testHasPrevious() {
        System.out.println("hasPrevious");
        MusicSystem instance = musicSystem;
        boolean expResult = true;
        boolean result = instance.hasPrevious();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasPause method, of class MusicSystem.
     */
    @Test
    public void testHasPause() {
        System.out.println("hasPause");
        MusicSystem instance = musicSystem;
        boolean expResult = true;
        boolean result = instance.hasPause();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasTracks method, of class MusicSystem.
     */
    @Test
    public void testHasTracks() {
        System.out.println("hasTracks");
        MusicSystem instance = musicSystem;
        boolean expResult = true;
        boolean result = instance.hasTracks();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasCurrentTime method, of class MusicSystem.
     */
    @Test
    public void testHasCurrentTime() {
        System.out.println("hasCurrentTime");
        MusicSystem instance = musicSystem;
        boolean expResult = true;
        boolean result = instance.hasCurrentTime();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTitle method, of class MusicSystem.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        MusicSystem instance = musicSystem;
        String expResult = "HiFi-Anlage";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setState method, of class MusicSystem.
     */
    @Test
    public void testSetState() {
        System.out.println("setState");
        MusicSystemState state = MusicSystemState.PAUSE;
        MusicSystem instance = musicSystem;
        instance.setMusicSystemState(state);
        assertEquals(MusicSystemState.PAUSE, instance.getMusicSystemState());
    }

    /**
     * Test of registerObserver method, of class MusicSystem.
     */
    @Test
    public void testRegisterObserver_TrackTimeObserver() {
        System.out.println("registerObserver");
        TrackTimeObserver o = (TrackTimeObserver) this;
        MusicSystem instance = musicSystem;
        instance.registerObserver(o);
        int anzTrackTimeObserverCalls = updateTrackTime;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyTrackTimeObservers();
        assertEquals(anzTrackTimeObserverCalls + 1, updateTrackTime);
    }

    /**
     * Test of removeObserver method, of class MusicSystem.
     */
    @Test
    public void testRemoveObserver_TrackTimeObserver() {
        System.out.println("removeObserver");
        TrackTimeObserver o = (TrackTimeObserver) this;
        MusicSystem instance = musicSystem;
        instance.removeObserver(o);
        int anzTrackTimeObserverCalls = updateTrackTime;
        ((AbstractMusicPlayer) instance.getActivePlayer()).notifyTrackTimeObservers();
        assertEquals(anzTrackTimeObserverCalls, updateTrackTime);
    }

    @Override
    public void updateTrackTime() {
        updateTrackTime++;
    }

    @Override
    public void updateTrack() {
        updateTrack++;
    }

    @Override
    public void updateRecord() {
        updateRecord++;
    }

    @Override
    public void updateMusicPlayer() {
        updateMusicPlayer++;
    }

    @Override
    public void updateVolume() {
        updateVolume++;
    }

    @Override
    public void updateState() {
        updateState++;
    }

}
