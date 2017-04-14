
package de.beinlich.markus.musicsystem.model;

import java.io.*;
import java.util.*;


public interface RecordInterface {


    /**
     *
     * @return
     */
    String getTitle();
    
    RecordDto getDto();
    
    byte[] getCover();
            
    List<PlayListComponentInterface> getTracks();
    
    PlayListComponentInterface getTrackById(int uid);

}
