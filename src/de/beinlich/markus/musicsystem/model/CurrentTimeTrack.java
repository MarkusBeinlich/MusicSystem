/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model;

import java.io.*;
import java.util.logging.*;

/**
 *
 * @author Markus Beinlich
 */
public class CurrentTimeTrack implements Serializable{
    private static int currentTimeTrack;
    
    public CurrentTimeTrack(int currentTimeTrack){
        CurrentTimeTrack.currentTimeTrack = currentTimeTrack;
    }

    /**
     * @return the currentTimeTrack
     */
    public int getCurrentTimeTrack() {
        return currentTimeTrack;
    }
    
    private synchronized void writeObject(java.io.ObjectOutputStream s){
        try {
            s.writeInt(currentTimeTrack);
        } catch (IOException ex) {
            Logger.getLogger(CurrentTimeTrack.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private synchronized void readObject(java.io.ObjectInputStream s) {
        try {
            CurrentTimeTrack.currentTimeTrack = s.readInt();
        } catch (IOException ex) {
            Logger.getLogger(CurrentTimeTrack.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
