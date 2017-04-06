/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public class RadioStation extends PlayListComponent {
    private String frequency;
    
    /**
     *
     */
    public RadioStation(int uid) {
        this(uid, "Blank");
    }
    
    /**
     *
     * @param title
     */
    public RadioStation(int uid, String title) {
        this (uid, title, "???");
    }
    
    /**
     *
     * @param title
     * @param frequency
     */
    public RadioStation(int uid, String title, String frequency) {
        this.setTitle(title);
        this.frequency = frequency;
        //uid muss eindeutig gesetzt sein, da es sonst probleme mit equals() gibt
        this.setUid(uid);

    }

    /**
     * @return the frequency
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    @Override
    public String toString() {
//        return  "\n"+ this.getTitle() + ": " + this.frequency;
        return  "\n"+ this.getTitle();
    }

}
