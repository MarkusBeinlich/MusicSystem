/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.gui;

import de.beinlich.markus.musicsystem.model.net.MusicSystemControllerInterface;
import de.beinlich.markus.musicsystem.model.*;

/**
 *
 * @author Markus Beinlich
 */
public class MusicServer1Start {

    public static void main(String args[]) {
        MusicSystem musicSystem = MusicSystem.getInstance("musicsystem");
        MusicSystemControllerInterface musicSystemController = new MusicSystemController(musicSystem);
    }
}
