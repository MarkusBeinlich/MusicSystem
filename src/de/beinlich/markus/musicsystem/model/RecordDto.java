package de.beinlich.markus.musicsystem.model;

import java.io.*;
import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public class RecordDto implements Serializable {

    public String title;
    public String artist;
    public String medium;
    public byte[] cover;
    public ArrayList<PlayListComponentDto> tracks;
}
