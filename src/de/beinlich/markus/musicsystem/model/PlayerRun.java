/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

/**
 *
 * @author Markus Beinlich
 * http://stackoverflow.com/questions/12548603/playing-audio-using-javafx-mediaplayer-in-a-normal-java-application
 */
class PlayerRun extends Thread {

    private final AbstractMusicPlayer player;

    PlayerRun(AbstractMusicPlayer player) {
        this.player = player;
    }

    @Override
    public void run() {
        int i = 0;
        while (player.getMusicSystemState() != MusicSystemState.STOP) {
            // Pause gedrückt -> Zeit läuft nicht weiter
            if (player.getMusicSystemState() != MusicSystemState.PAUSE) {
                // Track ist zu Ende -> nächsten Track aufrufen
                if ((i / 10) >= player.getCurrentTrack().getPlayingTime()) {
                    player.next();
                    i = 0;
                }
                // Record kann kann durch player next zu ende sein. Deswegen State nochmal abfragen
                if (player.getMusicSystemState() != MusicSystemState.STOP) {
                    // jede Sekunde die currentTimeTrack anpassen und eins nach oben zählen.
                    // dann passt es auch (halbwegs), wenn beim Track-wechsel die Zeit auf 0 gesetzt wurde
                    if (i % 10 == 0){
                        player.setCurrentTimeTrack(player.getCurrentTimeTrack() + 1);
                        i = player.getCurrentTimeTrack() * 10;
                    }
                    // alle 3 Sekunden die Zeit ausgeben
                    if (i++ % 30 == 0) {
                        System.out.println(System.currentTimeMillis() + player.getClass().getSimpleName() + " Track:" + player.getCurrentTrack().getTitle() + " Time: " + (i / 10));
                    }
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                interrupt();
            }

        }
    }

}
