/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model.net;

import de.beinlich.markus.musicsystem.model.*;

/**
 *
 * @author Markus
 */
public interface MusicSystemControllerInterface {

    public void play();

    public void pause();

    public void stop();

    public void previous();

    public void next();

    public void setVolume(double volume);

    void seek(int currentTimtTrack);

    public void setCurrentTrack(PlayListComponentInterface track);

    public void setActivePlayer(String selectedPlayer);

    public void setRecord(RecordInterface record);
}
