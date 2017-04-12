package de.beinlich.markus.musicsystem.model;

import java.io.*;
import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public class MusicPlayerDto implements Serializable {

    public String title;
    public MusicSystemState musicSystemState;
    public double volume;
    public PlayListComponentDto currentTrack;
    public int currentTimeTrack;
    public RecordDto record;
    public boolean hasPlay;
    public boolean hasStop;
    public boolean hasNext;
    public boolean hasPause;
    public boolean hasPrevious;
    public boolean hasTracks;
    public boolean hasCurrentTime;
}
