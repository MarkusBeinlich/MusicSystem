
package de.beinlich.markus.musicsystem.model;

import java.io.*;


public interface PlayListComponentInterface {


    /**
     *
     * @return
     */
    String getTitle();
    
    int getPlayingTime();
    
    PlayListComponentDto getDto();

}
