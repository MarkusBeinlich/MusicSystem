/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model.net;

import de.beinlich.markus.musicsystem.model.*;
import java.io.Serializable;

/**
 *
 * @author Markus
 */
public class ClientInit implements Serializable{
    private final MusicSystemDto musicSystem;
    private final MusicCollectionDto musicCollection;
    private final ServerPool serverPool;

    
    public ClientInit(MusicSystemDto musicSystem, MusicCollectionDto musicCollection, ServerPool serverPool){
        this.musicSystem = musicSystem;
        this.musicCollection = musicCollection;
        this.serverPool = serverPool;
    }
    /**
     * @return the musicSystem
     */
    public MusicSystemDto getMusicSystem() {
        return musicSystem;
    }

    /**
     * @return the musicCollection
     */
    public MusicCollectionDto getMusicCollection() {
        return musicCollection;
    }

    /**
     * @return the serverPool
     */
    public ServerPool getServerPool() {
        return serverPool;
    }
    
}
