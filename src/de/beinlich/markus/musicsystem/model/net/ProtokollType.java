/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model.net;

import de.beinlich.markus.musicsystem.model.ServerAddr;
import de.beinlich.markus.musicsystem.model.*;
import java.io.*;

/**
 *
 * @author Markus Beinlich
 */
public enum ProtokollType implements Serializable{
    MUSIC_COLLECTION_DTO(MusicCollectionDto.class),
    MUSIC_SYSTEM_DTO(MusicSystemDto.class), 
    MUSIC_PLAYER_DTO(MusicPlayerDto.class), 
    MUSIC_PLAYER_SELECTED(String.class),
    RECORD_DTO(RecordDto.class), 
    RECORD_SELECTED(RecordDto.class),
    STATE(MusicSystemState.class), 
    PLAY_LIST_COMPONENT_DTO(PlayListComponentDto.class), 
    VOLUME(Double.class),
    TRACK_TIME(Integer.class),
    TRACK_SELECTED(PlayListComponentDto.class),
    CLIENT_COMMAND_PLAY(MusicSystemState.class),
    CLIENT_COMMAND_NEXT(PlayListComponentDto.class),
    CLIENT_COMMAND_PREVIOUS(PlayListComponentDto.class),
    CLIENT_COMMAND_PAUSE(MusicSystemState.class),
    CLIENT_COMMAND_STOP(MusicSystemState.class),
    CLIENT_INIT(ClientInit.class),
    SERVER_POOL(ServerPool.class),
    SERVER_ADDR(ServerAddr.class),
    CLIENT_DISCONNECT(Boolean.class),
    SERVER_DISCONNECT(Boolean.class),
//    HAS_CURRENT_TIME(Boolean.class),
//    HAS_TRACKS(Boolean.class),
//    HAS_PAUSE(Boolean.class),
//    HAS_PREVIOUS(Boolean.class),
//    HAS_NEXT(Boolean.class),
    CLIENT_NAME(String.class);
    private final Class <?> classs;
    
    ProtokollType(Class classs){
        this.classs = classs;
    }

    /**
     * @return the classs
     */
    public Class <?> getClasss() {
        return classs;
    }
}