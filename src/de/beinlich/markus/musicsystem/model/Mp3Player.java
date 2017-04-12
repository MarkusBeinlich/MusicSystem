/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.model.db.*;
import javafx.embed.swing.*;
import javafx.scene.media.*;

/**
 *
 * @author Markus Beinlich
 */
class Mp3Player extends AbstractMusicPlayer {

    // cdStatus (play, pause, stop)
    // eventuell flag für change durch CdPlayer
    // Klasse für Play-Tread - wird durch play gestartet
    // durch stop beendet
    // durch CD-Ende beendet
    //ListIterator<Track> cdIterator;
    static final JFXPanel fxPanel = new JFXPanel();
    private transient MediaPlayer mp3Player;

    /**
     *
     */
    public Mp3Player() {
        this(new Record());
    }

    /**
     *
     * @param cd
     */
    public Mp3Player(Record cd) {
        if (cd == null) {
            setRecord(new Record());
        } else {
            setRecord(cd);
        }
//        String mediaSource;
//        mediaSource = "file:///" + this.getRecord().getTracks().get(0).getFileName().replace("\\", "/").replaceAll(" ", "%20");
//        System.out.println(System.currentTimeMillis() + "URI:" + mediaSource);
////        mp3Player = this.createPlayer(mediaSource);
//        mp3Player = new MediaPlayer(new Media(mediaSource));
        setMusicSystemState(MusicSystemState.STOP);
    }

    @Override
    public void setCurrentTrack(PlayListComponent currentTrack) {
        //am ende eines Track den usecounter auf der datenbank hochzählen
        if (this.getCurrentTrack() != null) {
            if (this.getCurrentTimeTrack() == currentTrack.getPlayingTime()) {
                try (DatabaseConnection dbc = new DatabaseConnection()) {
                    RecordCollectionConnector rcc = new RecordCollectionConnector(dbc);
                    rcc.incrementUseCounter(currentTrack.getUid());
                    System.out.println(System.currentTimeMillis() + "incremented" + currentTrack.getUid());
                }
            }
        }
        String mediaSource;
        mediaSource = "file:///" + currentTrack.getFileName().replace("\\", "/").replaceAll(" ", "%20");
        System.out.println(System.currentTimeMillis() + "URI:" + mediaSource);
//        mp3Player = this.createPlayer(mediaSource);
        if (mp3Player != null) {
            mp3Player.stop();
        }
        mp3Player = new MediaPlayer(new Media(mediaSource));
        if (getMusicSystemState() == MusicSystemState.PLAY) {
            this.play();
        }
        super.setCurrentTrack(currentTrack);
    }

    @Override
    public void play() {
        super.play();
        mp3Player.play(); 
        mp3Player.setVolume(getVolume() / 100);
    }

    @Override
    public void stop() {

        if (getRecord() != null && getRecord().getTracks().size()>0) {
            setCurrentTrack((PlayListComponent)getRecord().getTracks().get(0));
        }
        if (mp3Player != null) {
            mp3Player.stop();
        }
        super.stop();
    }

    @Override
    public void pause() {
        super.pause();
        mp3Player.pause();
    }

    @Override
    public void setVolume(double volume) {
        super.setVolume(volume);
        mp3Player.setVolume(volume / 100);
    }

    /**
     *
     */
    @Override
    public void previous() {
        // TODO Automatisch generierter Methodenstub
        //setCurrentTrack((getRecord().getTracks().length + getCurrentTrack() - 1) % getRecord().getTracks().length);
        int index = getRecord().getTracks().indexOf(getCurrentTrack());
        if (index > 0) {
            setCurrentTrack((PlayListComponent)getRecord().getTracks().get(index - 1));
        }
        System.out.println(System.currentTimeMillis() + "Mp3: " + getRecord().getTitle() + " Track: " + getCurrentTrack().getTitle() + " wird abgespielt.");
    }

    /**
     * @param cd das zu setzende Objekt cd
     */
    @Override
    public final void setRecord(Record cd) {
        System.out.println(System.currentTimeMillis() + "Mp3: " + cd.getTitle() + " wird eingelegt.");
        super.setRecord(cd);
        System.out.println(System.currentTimeMillis() + "Mp3: " + cd.getTitle() + " wurde eingelegt.");
    }

    @Override
    public boolean hasPlay() {
        return true;
    }

    @Override
    public boolean hasStop() {
        return true;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public boolean hasPrevious() {
        return true;
    }

    @Override
    public boolean hasPause() {
        return true;
    }

    @Override
    public boolean hasTracks() {
        return true;
    }

    @Override
    public boolean hasCurrentTime() {
        return true;
    }

    /**
     * @return a MediaPlayer for the given source which will report any errors
     * it encounters
     */
    private MediaPlayer createPlayer(String aMediaSrc) {
        System.out.println(System.currentTimeMillis() + "Creating player for: " + aMediaSrc);
        final MediaPlayer player = new MediaPlayer(new Media(aMediaSrc));
        player.setOnError(new Runnable() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis() + "Media error occurred: " + player.getError());
            }
        });
        return player;
    }

}
