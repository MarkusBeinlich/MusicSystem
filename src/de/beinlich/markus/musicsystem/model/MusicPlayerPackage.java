 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import java.io.*;

/**
 *
 * In diesem Interface wird festgelegt, welche Attribute und Methoden ein
 * MusicPlayer hat.
 *
 * Die has??? - Methoden geben an, ob ein konkretes Gerät eine Methode
 * unterstützt. zum Beispiel kennt ein Radio keine Pause-Methode.
 *
 * @author Markus Beinlich
 */
interface MusicPlayerPackage {

    /**
     *
     */


    /**
     *
     */
    void play();

    /**
     *
     */
    void pause();

    /**
     *
     */
    void next();

    /**
     *
     */
    void previous();

    /**
     *
     */
    void stop();

    /**
     *
     * @return
     */
    public abstract boolean hasPlay();

    /**
     *
     * @return
     */
    public abstract boolean hasStop();

    /**
     *
     * @return
     */
    public abstract boolean hasNext();

    /**
     *
     * @return
     */
    public abstract boolean hasPrevious();

    /**
     *
     * @return
     */
    public abstract boolean hasPause();

    /**
     *
     * @return
     */
    public abstract boolean hasTracks();

    /**
     *
     * @return
     */
    public abstract boolean hasCurrentTime();

    /**
     *
     * @return
     */
    MusicSystemState getMusicSystemState();

    /**
     *
     * @return
     */
    Record getRecord();

    /**
     *
     * @return
     */
    String getTitle();

    /**
     *
     * @return
     */
    PlayListComponent getCurrentTrack();

    /**
     *
     * @param record
     */
    void setRecord(Record record);

    /**
     *
     * @param
     */
    void setMusicSystemState(MusicSystemState state);

    void setVolume(double volume);

    double getVolume();

    /**
     *
     * @param track
     */
    void setCurrentTrack(PlayListComponent track);

    /**
     *
     * @return
     */
    int getCurrentTimeTrack();

    void registerObserver(VolumeObserver o);

    void removeObserver(VolumeObserver o);

    void registerObserver(TrackTimeObserver o);

    void removeObserver(TrackTimeObserver o);

    void registerObserver(TrackObserver o);

    void removeObserver(TrackObserver o);

    void registerObserver(StateObserver o);

    void removeObserver(StateObserver o);

    void registerObserver(RecordObserver o);

    void removeObserver(RecordObserver o);

    void registerObserver(MusicPlayerObserver o);

    void removeObserver(MusicPlayerObserver o);
}
