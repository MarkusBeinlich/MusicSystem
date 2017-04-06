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
public interface MusicSystemInterface {

    MusicPlayer getActivePlayer();

    Record getRecord();

    void setRecord(Record record);

    MusicSystemState getMusicSystemState();

    LinkedList<MusicPlayer> getSources();

    MusicPlayer getSource(String title);

    PlayListComponent getCurrentTrack();

    String getName();

    String getLocation();

    int getCurrentTimeTrack();

    double getVolume();

    void play();

    void pause();

    void next();

    void previous();

    void stop();

    void setVolume(double volume);

    void setCurrentTrack(PlayListComponent track);

    void setActiveSource(MusicPlayer activeSource) throws IllegaleSourceException;

    public ServerAddr getServerAddr();

    boolean hasPause();

    boolean hasPlay();

    boolean hasNext();

    boolean hasPrevious();

    boolean hasStop();

    boolean hasTracks();

    boolean hasCurrentTime();

    void registerObserver(TrackObserver o);

    void registerObserver(TrackTimeObserver o);

    void registerObserver(VolumeObserver o);

    void registerObserver(StateObserver o);

    void registerObserver(RecordObserver o);

    void registerObserver(MusicPlayerObserver o);

    void registerObserver(ServerPoolObserver o);
}
