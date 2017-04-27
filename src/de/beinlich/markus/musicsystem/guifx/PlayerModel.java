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
public class PlayerModel {

        private final ObservableList<MusicPlayerInterface> player;

        public PlayerModel(MusicSystemInterface musicSystem) {
            player = FXCollections.observableArrayList(musicSystem.getPlayers());
        }
        
        public ObservableList<MusicPlayerInterface> getPlayer() {
            return player;
        }
    }
