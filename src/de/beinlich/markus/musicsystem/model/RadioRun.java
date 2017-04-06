/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Markus
 */
public class RadioRun extends Thread {

    private final Radio player;

    RadioRun(Radio player) {
        this.player = player;
    }

    @Override
    public void run() {
        String mediaSource;
        mediaSource = ((RadioStation) player.getCurrentTrack()).getFrequency().replaceAll(" ", "%20");
//        mediaSource = "http://br-mp3-bayern3-s.akacast.akamaistream.net/7/464/142692/v1/gnl.akacast.akamaistream.net/br_mp3_bayern3_s";
//        mediaSource = "http://www.wdr.de/wdrlive/media/hls/1live-diggi.m3u8";
//        mediaSource = "file:///" + "C:/temp/Hello.mp3".replace("\\", "/").replaceAll(" ", "%20");
        System.out.println(System.currentTimeMillis() + "URI:" + mediaSource);
        URL url;
        try {
//            url = new URL("https://resource.track.mp3");
            url = new URL(mediaSource);

            try {
                player.setMp3(Files.createTempFile("now-playing", ".mp3"));

                try (InputStream stream = url.openStream()) {
                    player.setMp3Inputstream(stream);
                    Files.copy(stream, player.getMp3(), StandardCopyOption.REPLACE_EXISTING);
                } catch (SocketException ex) {
                    System.out.println("RadioRun - catch - SocketException: Cause=" + ex.getCause() + " Message:" + ex.getMessage());
                    //Socket close wir von Radio.stop() erzeugt und dient dem stoppen des Radiosenders
                    if (!ex.getMessage().equals("Socket closed")) {
                        Logger.getLogger(RadioRun.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (IOException ex) {
                    System.out.println("RadioRun - catch");
                    //Stream close wir von Radio.stop() erzeugt und dient dem stoppen des Radiosenders
                    if (!ex.getMessage().equals("Stream closed")) {
                        Logger.getLogger(RadioRun.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(RadioRun.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(RadioRun.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
