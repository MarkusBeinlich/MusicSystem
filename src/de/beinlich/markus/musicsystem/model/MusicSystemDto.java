package de.beinlich.markus.musicsystem.model;

import java.io.*;
import java.util.*;

/**
 *
 * @author Markus Beinlich
 */
public class MusicSystemDto implements Serializable {

    public String musicSystemName;
    public boolean power;
    public boolean onOffSwitch;
    public String location;
    public MusicPlayerDto activePlayer;
    public ServerAddr serverAddr;
    public LinkedList<MusicPlayerDto> players;
}
