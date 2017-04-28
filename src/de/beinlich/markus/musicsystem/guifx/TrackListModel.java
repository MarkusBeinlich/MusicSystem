/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.guifx;

import de.beinlich.markus.musicsystem.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Markus
 */
public class TrackListModel {

    private ObservableList<PlayListComponentInterface> tracks;

    public TrackListModel(MusicSystemInterface musicSystem) {
        tracks = FXCollections.observableArrayList(musicSystem.getRecord().getTracks());
    }

    public ObservableList<PlayListComponentInterface> getTracks() {
        return tracks;
    }

    public void setTracks(ObservableList<PlayListComponentInterface> tracks) {
        this.tracks = tracks;
    }

}
