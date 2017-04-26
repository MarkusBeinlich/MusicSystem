/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.guifx;

import de.beinlich.markus.musicsystem.model.*;
import de.beinlich.markus.musicsystem.model.net.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Markus
 */
public class ServerModel {

        private final ObservableList<String> server;

        public ServerModel(MusicClient musicClient) {
            server = FXCollections.observableArrayList(musicClient.getServerPool().getActiveServers());
        }
        
        public ObservableList<String> getServer() {
            return server;
        }
    }

