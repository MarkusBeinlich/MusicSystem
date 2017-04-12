package de.beinlich.markus.musicsystem.model;

import java.io.*;
import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public class RecordDto implements RecordInterface, Serializable {

    private static final long serialVersionUID = -5271344418912579109L;

    public String title;
    public String artist;
    public String medium;
    public byte[] cover;
    public ArrayList<PlayListComponentDto> tracks;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public RecordDto getDto() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getCover() {
        return cover;
    }

    @Override
    public List<PlayListComponentInterface> getTracks() {
        List<PlayListComponentInterface> tracks = new ArrayList<>();
        for (PlayListComponentDto track : this.tracks) {
            tracks.add(track);
        }
        return tracks;
    }

    @Override
    public String toString() {
        return this.artist + " - " + this.getTitle();
    }
}
