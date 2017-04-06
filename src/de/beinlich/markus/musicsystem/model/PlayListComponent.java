/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public abstract class PlayListComponent implements Serializable {

    private String title;
    private int playingTime;
    private int uid;
    private String fileName;

    /**
     *
     * @param menuComponent
     */
    public void add(PlayListComponent menuComponent) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param menuComponent
     */
    public void remove(PlayListComponent menuComponent) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param i
     * @return
     */
    public PlayListComponent getChild(int i) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return
     */
    public int getUid() {
        return uid;
    }

    /**
     *
     * @return
     */
    public int getPlayingTime() {
        return playingTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayListComponent) {
            PlayListComponent tr = (PlayListComponent) obj;
            return (uid == tr.uid);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.uid;
        return hash;
    }

    @Override
    public String toString() {
        return this.getTitle() + ": " + this.getPlayingTime() + " sec";
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param playingTime the playingTime to set
     */
    public void setPlayingTime(int playingTime) {
        this.playingTime = playingTime;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(int uid) {
        this.uid = uid;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
