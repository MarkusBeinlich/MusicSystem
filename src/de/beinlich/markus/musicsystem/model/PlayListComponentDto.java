package de.beinlich.markus.musicsystem.model;

import java.io.*;
import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public class PlayListComponentDto implements PlayListComponentInterface, Serializable {

    private static final long serialVersionUID = 3730695288138344046L;

    public String title;
    public int playingTime;
    public int uid;
    public String fileName;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getPlayingTime() {
        return playingTime;
    }

    @Override
    public PlayListComponentDto getDto() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String toString() {
        return this.getTitle() + ": " + this.getPlayingTime() + " sec";
    }
}
