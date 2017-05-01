/*

//
//JavaFX 2 does not support mp3 streams, but you can hack it.
//
//Open a socket connection to any ICY mp3 stream in a new thread and start saving the bytes to a file.
//
//After a few kilobytes received open the file in the JavaFX MediaPlayer and play it, but do not stop receiving bytes on the another thread.
//
//This is what worked for me.

 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.lib.MusicSystemState;
import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.*;
import javafx.scene.media.*;

/**
 *
 * @author Markus Beinlich
 */
class Radio extends AbstractMusicPlayer {

    static final JFXPanel fxPanel = new JFXPanel();
    private transient MediaPlayer mp3Player;
    private transient Path mp3;
    private transient InputStream mp3Inputstream;
    private transient RadioRun radioRun;

    /**
     *
     */
    public Radio() {
//        MusicCollection rsc = RadioStationCollection.getInstance();
        //sicherstellen, dass auch der Record initialisiert ist
        setRecord(new Record());
//        radioStation = (RadioStation) rsc.getRecord().getTracks().get(0);
        setMusicSystemState(MusicSystemState.STOP);
    }

    @Override
    public void play() {
        String mediaSource;
//        mediaSource =  "http://br-mp3-bayern3-s.akacast.akamaistream.net/7/464/142692/v1/gnl.akacast.akamaistream.net/br_mp3_bayern3_s".replaceAll(" ", "%20");
//        mediaSource = "http://streams.br.de/bayern3_1.m3u";
//        System.out.println(System.currentTimeMillis() + "URI:" + mediaSource);
        System.out.println(System.currentTimeMillis() + this.getTitle() + ": " + getRecord().getTitle() + " Track: " + getCurrentTrack().getTitle() + " wird abgespielt.");
        //radioRun muss nur gestartet werden, wenn noch keine Instanz läuft.
        //In radioRun wird das mp3-Radioprogramm in eine Datei gestreamt.
        //Diese Datei dient dem mp3Player dann als Eingabe
        if (radioRun == null || radioRun.getState() == Thread.State.TERMINATED) {
            radioRun = new RadioRun(this);
            radioRun.start();
        } else {
            return;
        }
        System.out.println(System.currentTimeMillis() + "sleep 3 second for radio playing");
        try {
            //        mp3Player = this.createPlayer(mediaSource);
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Radio.class.getName()).log(Level.SEVERE, null, ex);
        }
        Media media = new Media(mp3.toFile().toURI().toString());
        if (mp3Player != null) {
            mp3Player.stop();
        }
        mp3Player = this.createPlayer(media);
        System.out.println(System.currentTimeMillis() + "start radio playing");
        mp3Player.play();
        mp3Player.setVolume(getVolume() / 100);
        setMusicSystemState(MusicSystemState.PLAY);
    }

    @Override
    public void stop() {
        super.stop();
        if (mp3Player != null) {
            mp3Player.stop();
        }
        //hier fällt mir nichts besseres ein, da der Thread ja dauerhaft mit Dateischreiben beschäftigt ist.
        //????? Eventuell ein close auf den InputStream ?????????????
        if (radioRun != null) {
            try {
                mp3Inputstream.close();
//            radioRun.stop();
            } catch (IOException ex) {
                System.out.println("RadioStream closed - catch");
                Logger.getLogger(Radio.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                System.out.println("RadioStream closed - finally");
            }
        }
    }

    @Override
    public void setCurrentTrack(PlayListComponent currentTrack) {
        super.setCurrentTrack(currentTrack);
        //den aktuellen Radiosender stoppen
        if (MusicSystemState.STOP != this.getMusicSystemState()) {
            stop();
        }
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
    public void pause() {
        // TODO Automatisch generierter Methodenstub
        System.out.println(System.currentTimeMillis() + "Pause geht beim Radio nicht.");
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasPlay() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasStop() {
        return true;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasPrevious() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasPause() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasTracks() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasCurrentTime() {
        return false;
    }

    @Override
    public void setCurrentTimeTrack(int currentTimeTrack) {
        //nichts tun, damit der TrackObserver nicht unnötig aufgerufen wird
    }

    @Override
    public int getCurrentTimeTrack() {
        return 0;
    }

    /**
     * @return the mp3
     */
    public Path getMp3() {
        return mp3;
    }

    /**
     * @param mp3 the mp3 to set
     */
    public void setMp3(Path mp3) {
        this.mp3 = mp3;
    }

    /**
     * @return a MediaPlayer for the given source which will report any errors
     * it encounters
     */
    private MediaPlayer createPlayer(Media media) {

        final MediaPlayer player = new MediaPlayer(media);
        player.setOnError(new Runnable() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis() + "Media error occurred: " + player.getError());
            }
        });
        return player;
    }

    /**
     * @param mp3Inputstream the mp3Inputstream to set
     */
    public void setMp3Inputstream(InputStream mp3Inputstream) {
        this.mp3Inputstream = mp3Inputstream;
    }

}
