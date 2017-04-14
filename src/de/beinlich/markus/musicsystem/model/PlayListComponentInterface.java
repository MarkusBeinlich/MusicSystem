
package de.beinlich.markus.musicsystem.model;

import java.io.*;


public interface PlayListComponentInterface {


    /**
     *
     * @return
     */
    String getTitle();
    
    int getUid();
    
    int getPlayingTime();
    
    PlayListComponentDto getDto();

}
