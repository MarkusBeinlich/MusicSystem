/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.gui;

//import de.beinlich.markus.musicsystem.gui.MusicServerApp;
import de.beinlich.markus.musicsystem.model.*;
import de.beinlich.markus.musicsystem.model.net.MusicSystemControllerInterface;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.*;

/**
 *
 * @author Markus
 */
public class MusicSystemController implements MusicSystemControllerInterface {

    MusicSystemInterface musicSystem;
    MusicServerApp musicServerApp;

    public MusicSystemController(MusicSystemInterface musicSystem) {
        this.musicSystem = musicSystem;
        try {
            musicServerApp = MusicServerApp.startMusicServerApp(this, musicSystem);
        } catch (InterruptedException ex) {
            Logger.getLogger(MusicSystemController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(MusicSystemController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void play() {
        musicSystem.play();
    }

    @Override
    public void pause() {
        musicSystem.pause();
    }

    @Override
    public void stop() {
        musicSystem.stop();
    }

    @Override
    public void previous() {
        musicSystem.previous();
    }

    @Override
    public void next() {
        musicSystem.next();
    }

    @Override
    public void setVolume(double volume) {
        musicSystem.setVolume(volume);
    }

    @Override
    public void setCurrentTrack(PlayListComponentInterface track) {
        if (track instanceof PlayListComponent) {
            musicSystem.setCurrentTrack((PlayListComponent) track);
        } else if (track instanceof PlayListComponentDto) {
            musicSystem.setCurrentTrack(musicSystem.getRecord().getTrackById(track.getUid()));
        } else {
            throw new ClassCastException(track.getClass().getName());
        }
    }

    @Override
    public void setActivePlayer(String selectedPlayer) {
        try {
            musicSystem.setActivePlayer(musicSystem.getPlayer(selectedPlayer));
            MusicCollectionInterface musicCollection = MusicCollection.getInstance(musicSystem.getActivePlayer().getClass().getSimpleName());
            musicSystem.setRecord((Record) musicCollection.getRecord(0));
        } catch (IllegalePlayerException ex) {
            Logger.getLogger(MusicServerApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setRecord(RecordInterface record) {
        MusicCollectionInterface musicCollection = MusicCollection.getInstance(musicSystem.getActivePlayer().getClass().getSimpleName());
        if (record instanceof Record) {
            musicSystem.setRecord((Record) record);
        } else if (record instanceof RecordDto) {
            musicSystem.setRecord((Record) musicCollection.getRecordById(((RecordDto) record).rid));
        } else {
            throw new ClassCastException(record.getClass().getName());
        }
    }
}
