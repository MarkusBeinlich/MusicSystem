/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.lib.StateObserver;
import de.beinlich.markus.musicsystem.lib.MusicPlayerObserver;
import de.beinlich.markus.musicsystem.lib.VolumeObserver;
import de.beinlich.markus.musicsystem.lib.TrackTimeObserver;
import de.beinlich.markus.musicsystem.lib.RecordObserver;
import de.beinlich.markus.musicsystem.lib.TrackObserver;
import de.beinlich.markus.musicsystem.lib.MusicPlayerInterface;
import de.beinlich.markus.musicsystem.lib.MusicPlayerDto;
import de.beinlich.markus.musicsystem.lib.MusicSystemState;
import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
abstract class AbstractMusicPlayer implements MusicPlayerInterface, MusicPlayerPackage {

    private String title = this.getClass().getSimpleName();
    private MusicSystemState musicSystemState;
    private double volume = 20.0;
    private transient final ArrayList<VolumeObserver> volumeObservers = new ArrayList<>();
    private transient final ArrayList<TrackTimeObserver> trackTimeObservers = new ArrayList<>();
    private transient final ArrayList<TrackObserver> trackObservers = new ArrayList<>();
    private transient final ArrayList<StateObserver> stateObservers = new ArrayList<>();
    private transient final ArrayList<RecordObserver> recordObservers = new ArrayList<>();
    private PlayListComponent currentTrack;
    private int currentTimeTrack;
    private Record record;
    private transient PlayerRun cdRun;

    @Override
    public MusicPlayerDto getDto() {
        MusicPlayerDto musicPlayerDto = new MusicPlayerDto();
        musicPlayerDto.title = this.title;
        musicPlayerDto.musicSystemState = this.musicSystemState;
        musicPlayerDto.volume = this.volume;
        musicPlayerDto.currentTrack = (this.currentTrack == null) ? null : this.currentTrack.getDto();
        musicPlayerDto.currentTimeTrack = this.currentTimeTrack;
        musicPlayerDto.record = this.record.getDto();
        musicPlayerDto.hasPlay = hasPlay();
        musicPlayerDto.hasStop = hasStop();
        musicPlayerDto.hasNext = hasNext();
        musicPlayerDto.hasPause = hasPause();
        musicPlayerDto.hasPrevious = hasPrevious();
        musicPlayerDto.hasTracks = hasTracks();
        musicPlayerDto.hasCurrentTime = hasCurrentTime();
        return musicPlayerDto;
    }

    /**
     *
     * @return
     */
    @Override
    public abstract boolean hasPlay();

    /**
     *
     * @return
     */
    @Override
    public abstract boolean hasStop();

    /**
     *
     * @return
     */
    @Override
    public abstract boolean hasNext();

    /**
     *
     * @return
     */
    @Override
    public abstract boolean hasPrevious();

    /**
     *
     * @return
     */
    @Override
    public abstract boolean hasPause();

    @Override
    public abstract boolean hasTracks();

    /**
     *
     * @return
     */
    @Override
    public abstract boolean hasCurrentTime();

    /**
     *
     */
    @Override
    public void play() {
        System.out.println(System.currentTimeMillis() + this.getTitle() + ": " + getRecord().getTitle()
                + " Track: " + getCurrentTrack().getTitle() + " wird abgespielt. Volume: " + getVolume());
        //cdRun muss nur gestartet werden, wenn noch kein Instanz läuft.
        if (cdRun == null || cdRun.getState() == Thread.State.TERMINATED) {
            cdRun = new PlayerRun(this);
            cdRun.start();
        }
        setMusicSystemState(MusicSystemState.PLAY);
    }

    /**
     *
     */
    @Override
    public void pause() {
        // TODO Automatisch generierter Methodenstub
        setMusicSystemState(MusicSystemState.PAUSE);
        System.out.println(System.currentTimeMillis() + this.getClass().getSimpleName() + " ist angehalten.");
    }

    /**
     *
     */
    @Override
    public void next() {
        // TODO Automatisch generierter Methodenstub
        //setCurrentTrack((getCurrentTrack() + 1) % getRecord().getTracks().length);
        //if (cdIterator.hasNext()) {
        int index = getRecord().getTracks().indexOf(getCurrentTrack());
        if (getRecord().getTracks().size() > (1 + index)) {
            setCurrentTrack((PlayListComponent) getRecord().getTracks().get(index + 1));
            System.out.println(System.currentTimeMillis() + "Record: " + getRecord().getTitle() + " Track: " + getCurrentTrack().getTitle() + " wird abgespielt.");
        } else {
            setMusicSystemState(MusicSystemState.STOP);
            setCurrentTrack((PlayListComponent) getRecord().getTracks().get(0));
            System.out.println(System.currentTimeMillis() + "Record: " + getRecord().getTitle() + "beendet.");
        }
    }

    /**
     *
     */
    @Override
    public void stop() {
        setMusicSystemState(MusicSystemState.STOP);
//      Das gibt beim Radio keinen Sinn. Sonst wird jedesmal auf den ersten Sender gesprungen, wenn der Track gewechselt wird.
//        if (record != null) {
//            setCurrentTrack(record.getTracks().get(0));
//        }
        System.out.println(System.currentTimeMillis() + this.getClass().getSimpleName() + " ist gestoppt.");
    }

    /**
     *
     */
    @Override
    public void previous() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the state
     */
    @Override
    public MusicSystemState getMusicSystemState() {
        return musicSystemState;
    }

    /**
     * @param musicSystemState the state to set
     */
    @Override
    public final void setMusicSystemState(MusicSystemState musicSystemState) {
        this.musicSystemState = musicSystemState;
        notifyStateObservers();
    }

    /**
     * @return record
     */
    @Override
    public Record getRecord() {
        return record;
    }

    /**
     * @return the currentTrack
     */
    public PlayListComponent getCurrentTrack() {
        return currentTrack;
    }

    private static void showTrace() {
        for (StackTraceElement trace : Thread.currentThread().getStackTrace()) {
            System.out.println(trace);
        }
    }

    /**
     * @param currentTrack the currentTrack to set
     */
    @Override
    public void setCurrentTrack(PlayListComponent currentTrack) {
        System.out.println(System.currentTimeMillis() + "setCurrentTrack: " + currentTrack);
        //showTrace();

        this.currentTrack = currentTrack;
        this.currentTimeTrack = 0;
        notifyTrackObservers();
    }

    /**
     *
     * @param record
     */
    @Override
    public void setRecord(Record record) {
        //Stop aufrufen ist besser, als den Status auf Stop zu setzen.
        //Durch Polymorphie wird z.B. beim Radio der RadioRun Prozess gestoppt.
        stop();
        this.record = record;
        //
        if (record != null && record.getTracks().size() > 0) {
            setCurrentTrack((PlayListComponent) record.getTracks().get(0));
        }
        notifyRecordObservers();
    }

    @Override
    public int getCurrentTimeTrack() {
        return currentTimeTrack;
    }

    void seek(int currentTimeTrack) {
        setCurrentTimeTrack(currentTimeTrack);
    }

    void setCurrentTimeTrack(int currentTimeTrack) {
        this.currentTimeTrack = currentTimeTrack;
        notifyTrackTimeObservers();
    }

    public void notifyStateObservers() {
//        System.out.println(System.currentTimeMillis() + "notifyStateObservers: " + stateObservers.size());
        if (stateObservers != null) {
            for (int i = 0; i < stateObservers.size(); i++) {
                StateObserver observer = (StateObserver) stateObservers.get(i);
                observer.updateState();
            }
        }
    }

    /**
     *
     */
    public void notifyTrackObservers() {
        if (trackObservers != null) {
            for (int i = 0; i < trackObservers.size(); i++) {
                TrackObserver observer = (TrackObserver) trackObservers.get(i);
                observer.updateTrack();
            }
        }
    }

    /**
     *
     */
    public void notifyTrackTimeObservers() {
        if (trackTimeObservers != null) {
            for (int i = 0; i < trackTimeObservers.size(); i++) {
                TrackTimeObserver observer = (TrackTimeObserver) trackTimeObservers.get(i);
                observer.updateTrackTime();
            }
        }
    }

    public void notifyVolumeObservers() {
        if (volumeObservers != null) {
            for (int i = 0; i < volumeObservers.size(); i++) {
                VolumeObserver observer = (VolumeObserver) volumeObservers.get(i);
                observer.updateVolume();
            }
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(TrackTimeObserver o) {
        if (trackTimeObservers.contains(o) == false) {
            trackTimeObservers.add(o);
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void removeObserver(TrackTimeObserver o) {
        int i = trackTimeObservers.indexOf(o);
        if (i >= 0) {
            trackTimeObservers.remove(i);
        }
    }

    @Override
    public void registerObserver(VolumeObserver o) {
        if (volumeObservers.contains(o) == false) {
            volumeObservers.add(o);
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void removeObserver(VolumeObserver o) {
        int i = volumeObservers.indexOf(o);
        if (i >= 0) {
            volumeObservers.remove(i);
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(TrackObserver o) {
        if (trackObservers.contains(o) == false) {
            trackObservers.add(o);
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void removeObserver(TrackObserver o) {
        int i = trackObservers.indexOf(o);
        if (i >= 0) {
            trackObservers.remove(i);
        }
    }

    @Override
    public void registerObserver(StateObserver o) {
        if (stateObservers.contains(o) == false) {
            stateObservers.add(o);
        }
    }

    @Override
    public void removeObserver(StateObserver o) {
        int i = stateObservers.indexOf(o);
        if (i >= 0) {
            stateObservers.remove(i);
        }
    }

    /**
     *
     */
    public void notifyRecordObservers() {
        if (recordObservers != null) {
            System.out.println(System.currentTimeMillis() + "notifyRecordObservers:" + recordObservers.size());
            for (int i = 0; i < recordObservers.size(); i++) {
                RecordObserver observer = (RecordObserver) recordObservers.get(i);
                observer.updateRecord();
            }
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(RecordObserver o) {
        if (recordObservers.contains(o) == false) {
            recordObservers.add(o);
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void removeObserver(RecordObserver o) {
        int i = recordObservers.indexOf(o);
        if (i >= 0) {
            recordObservers.remove(i);
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(MusicPlayerObserver o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param o
     */
    @Override
    public void removeObserver(MusicPlayerObserver o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return  title;
    }

    /**
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the volume
     */
    public double getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(double volume) {
        this.volume = volume;
        notifyVolumeObservers();
    }
    
    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MusicPlayerInterface other = (MusicPlayerInterface) obj;
        return this.title.equals(other.getTitle());
    }

}
