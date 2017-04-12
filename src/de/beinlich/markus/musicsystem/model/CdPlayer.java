/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.model.db.*;

/**
 *
 * @author Markus Beinlich
 */
class CdPlayer extends AbstractMusicPlayer {

    // cdStatus (play, pause, stop)
    // eventuell flag für change durch CdPlayer
    // Klasse für Play-Tread - wird durch play gestartet
    // durch stop beendet
    // durch CD-Ende beendet
    //ListIterator<Track> cdIterator;
    /**
     *
     */
    public CdPlayer() {
        this(new Record());
    }

    /**
     *
     * @param cd
     */
    public CdPlayer(Record cd) {
        if (cd == null) {
            setRecord(new Record());
        } else {
            setRecord(cd);
        }
        setMusicSystemState(MusicSystemState.STOP);
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
        System.out.println(System.currentTimeMillis() + "CD: " + getRecord().getTitle() + " Track: " + getCurrentTrack().getTitle() + " wird abgespielt.");
    }

    /**
     * @param cd das zu setzende Objekt cd
     */
    @Override
    public final void setRecord(Record cd) {
        System.out.println(System.currentTimeMillis() + "CD: " + ((cd == null)? "null" : cd.getTitle()) + " wird eingelegt.");
        super.setRecord(cd);
        System.out.println(System.currentTimeMillis() + "CD: " + ((cd == null)? "null" : cd.getTitle()) + " wurde eingelegt.");
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

        super.setCurrentTrack(currentTrack);
    }

    @Override
    public void stop() {

        if (getRecord() != null && getRecord().getTracks().size()>0) {
            setCurrentTrack((PlayListComponent)getRecord().getTracks().get(0));
        }
        super.stop();
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

}
