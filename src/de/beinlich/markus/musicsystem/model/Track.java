package de.beinlich.markus.musicsystem.model;

import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public class Track extends PlayListComponent {
	private String title;
	
    /**
     *
     * @param title
     * @param playingTime
     * @param uid
     * @param fileName
     */
    public Track(String title, int playingTime, int uid, String fileName){
		this.setTitle(title);
		this.setPlayingTime(playingTime);
                this.setUid(uid);
                this.setFileName(fileName);
	}
	
}
