/*
 */
package de.beinlich.markus.musicsystem.model;

import java.io.*;
import java.util.*;
import javax.script.*;
import jdk.nashorn.api.scripting.*;

/**
 * Ein MusicSystem ist der zentrale Teil einer Musikanlage. Hier kann
 * beispielsweise die Lautstärke geregelt werden. Als Musik-Quelle dienen eine
 * oder mehrere MusicPlayer.
 *
 * Eine dieser MusicPlayer ist die activeSource. Die Methoden zur Steuerung der
 * Musikwiedergabe (Play, Next, ...) beziehen sich auf die activeSource.
 *
 * Die Klasse stellt mehrere Observable Verfügung: 1. MusicPlayerObserver: Es
 * werden alle Observer informiert, wenn sich die activeSource ändert. 2.
 * RecordObserver: Es werden alle Observer informiert, wenn sich der aktuelle
 * Record ändert. 3. TrackObserver: Es werden alle Observer informiert, wenn
 * sich der aktuelle Track ändert. 4. TrackTimeObserver: Es werden alle Observer
 * informiert, wenn sich die aktuelle TrackTime ändert.
 *
 * @author Markus Beinlich
 */
public class MusicSystem extends ElectricalDevice implements MusicSystemInterface {

    private static MusicSystem uniqueInstance;
    private MusicPlayerPackage activeSource;
//    private int port;
//    private String server_ip;
    private ServerAddr serverAddr;
    final private LinkedList<MusicPlayer> sources;
    private transient final ArrayList<MusicPlayerObserver> musicPlayerObservers = new ArrayList<>();

    /**
     *
     * @param name
     */
    private MusicSystem() {
        this("musicsystem");
    }

    private MusicSystem(String name) {
        this(name, "Sonstwo");
    }

    /**
     *
     * @param name
     * @param location
     */
    private MusicSystem(String name, String location) {
        this(name, location, false);
    }

    /**
     *
     * @param name
     * @param location
     * @param power
     */
    private MusicSystem(String name, String location, boolean power) {
        this(name, location, power, false);
    }

    /**
     *
     * @param name
     * @param location
     * @param power
     * @param onOffSwitch
     */
    public MusicSystem(String name, String location, boolean power, boolean onOffSwitch) {
        super(name, location, power, onOffSwitch);
        this.sources = new LinkedList<>();
    }

    public static synchronized MusicSystem getInstance(String name) {
        if (uniqueInstance == null) {
            uniqueInstance = new MusicSystem();
            uniqueInstance.readConfiguration(name);
        }
        return uniqueInstance;
    }

    /**
     * Aufbau eines MusicSystem mit Hilfe einer .json Datei.
     *
     * Die Parameter name und location des MusicSystem werden gesetzt. Im
     * Parameter musicplayers können beliebig viele MusicPlayer's angegeben
     * werden. Vom GUI werden allerdings maximal 4 unterstützt. Für jede
     * MusicPlayer muß ein type angegeben werden. Der type kann die Werte
     * CdPlayer, RecordPlayer oder Radio haben. Andere Werte werden ignoriert.
     * Je MusicPlayer kann optional ein title angegeben werden.
     *
     *
     * Beispiel: musicsystem = { "name":"HiFi-Anlage", "location":"Wohnzimmeer",
     * "musicplayers":[ { "type":"CdPlayer", "title":"CD-Player" }, {
     * "type":"RecordPlayer", "title":"Plattenspieler" }, { "type":"Radio",
     * "title":"UKW-Radio" } ] }
     * @param name
     */
    public void readConfiguration(String name) {
        File file;
        int port = 50_000;
        String fileName;
        String server_ip = "localhost";

        if (name == null) {
            fileName = "musicsystem.json";
        } else {
            fileName = name + ".json";
        }
        file = new File(fileName);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        try {
            engine.eval(new FileReader(file));
        } catch (ScriptException ex) {
            System.out.println(ex);
        } catch (FileNotFoundException ex) {
            System.out.println(System.currentTimeMillis() + "FileNotFound" + ex);
        }
        JSObject obj = (JSObject) engine.get("musicsystem");
        JSObject ms = (JSObject) obj.getMember("musicplayers");
        if (obj.hasMember("name")) {
            this.setName((String) obj.getMember("name"));
        }
        if (obj.hasMember("location")) {
            this.setLocation((String) obj.getMember("location"));
        }
        if (obj.hasMember("port")) {
            port = Integer.parseInt((String) obj.getMember("port"));
        }
        if (obj.hasMember("server_ip")) {
            server_ip = (String) obj.getMember("server_ip");
        }
        this.serverAddr = new ServerAddr(port, server_ip, this.getName(), true);
        this.setPower(true);
        this.setOnOffSwitch(true);

        for (int m = 0; ms.hasSlot(m); m++) {
            JSObject msSl = (JSObject) ms.getSlot(m);
            if (msSl.hasMember("type")) {
                switch ((String) msSl.getMember("type")) {
                    case "CdPlayer":
                        CdPlayer cdPlayer = new CdPlayer();
                        cdPlayer.setTitle((msSl.hasMember("title")) ? (String) msSl.getMember("title") : "CD");
                        this.addSource(cdPlayer);
                        break;
                    case "Mp3Player":
                        Mp3Player mp3Player = new Mp3Player();
                        mp3Player.setTitle((msSl.hasMember("title")) ? (String) msSl.getMember("title") : "Mp3");
                        this.addSource(mp3Player);
                        break;
                    case "RecordPlayer":
                        RecordPlayer recordPlayer = new RecordPlayer();
                        recordPlayer.setTitle((msSl.hasMember("title")) ? (String) msSl.getMember("title") : "Plattenspieler");
                        this.addSource(recordPlayer);
                        break;
                    case "Radio":
                        Radio radio = new Radio();
                        radio.setTitle((msSl.hasMember("title")) ? (String) msSl.getMember("title") : "Radio");
                        this.addSource(radio);
                        break;
                    default:
                        System.out.println(System.currentTimeMillis() + "Unbekannter type" + (String) msSl.getMember("type"));
                }
            }
        }
        try {
            this.setActiveSource(this.getSources().getFirst());
        } catch (IllegaleSourceException ex) {
            System.out.println(System.currentTimeMillis() + "Gerät kann nicht verwendet werden. Fehler: " + ex.getMessage());
        }

    }

    /**
     *
     * @return
     */
    @Override
    public double getVolume() {
        return activeSource.getVolume();
    }

    /**
     * @param volume the volume to set
     */
    @Override
    public void setVolume(double volume) {
        if (volume > 100) {
            System.out.println(System.currentTimeMillis() + "Die maximale Lautstärke ist 100.");
            activeSource.setVolume(100.0);
        } else if (volume < 0) {
            System.out.println(System.currentTimeMillis() + "Die minimal Lautstärke ist 0.");
            activeSource.setVolume(0.0);
        } else {
            activeSource.setVolume(volume);
        }
        System.out.println(System.currentTimeMillis() + "Lautstärke: " + getVolume());
    }

    @Override
    public void play() {
        boolean ok = true;
        if (activeSource == null) {
            System.out.println(System.currentTimeMillis() + "Sie müssen erst mit 'setActiveSource(???) eine Music-Quelle angeben.");
            ok = false;
        }
        if (this.isPower() == false) {
            System.out.println(System.currentTimeMillis() + "Bitte erst für Strom sorgen. Gerät kann nicht eingeschaltet werden.");
            ok = false;
        }
        if (this.isOnOffSwitch() == false) {
            System.out.println(System.currentTimeMillis() + "Bitte das Gerät erst einschalten.");
            ok = false;
        }
        if (ok == true) {
            activeSource.play();
        }
    }

    @Override
    public void pause() {
        activeSource.pause();
    }

    @Override
    public void stop() {
        activeSource.stop();
    }

    @Override
    public void previous() {
        activeSource.previous();
    }

    @Override
    public void next() {
        activeSource.next();
    }

    /**
     * @return the activeSource
     */
    @Override
    public MusicPlayer getActivePlayer() {
        return (MusicPlayer) activeSource;
    }

    /**
     * @param activeSource the activeSource to set
     * @throws de.beinlich.markus.musicsystem.model.IllegaleSourceException
     */
    @Override
    public void setActiveSource(MusicPlayer activeSource) throws IllegaleSourceException {
        //prüfen, ob die zu aktivierende Source zu den vorhandenen Sourcen gehört
        if (-1 == getSources().indexOf(activeSource)) {
            throw new IllegaleSourceException("Source " + activeSource + " is not part of sources: " + getSources());
        }
        //prüfen, ob es bereits eine aktive Source gibt und diese gegebenfalls stoppen
        if (this.activeSource != null) {
            this.activeSource.stop();
        }
        this.activeSource = (MusicPlayerPackage) activeSource;

        notifyMusicPlayerObservers();
    }

    /**
     * @param source the activeSource to set
     */
    private void addSource(MusicPlayer source) {
//        source.stop();
        sources.add(source);
        notifyMusicPlayerObservers();
    }

    @Override
    public String toString() {
        String as = (this.getActivePlayer() == null) ? "" : "\nAktive Quelle:" + this.getActivePlayer().getClass().getSimpleName();
        return "\nGerätename: " + this.getName()
                + "\nStandort: " + this.getLocation()
                + "\nStrom vorhanden: " + isPower()
                + "\nGerät eingeschaltet:" + isOnOffSwitch()
                + as
                + "\nQuellen: " + this.getSources().toString();
    }

    @Override
    public MusicSystemState getMusicSystemState() {
        return activeSource.getMusicSystemState();
    }

    @Override
    public Record getRecord() {
        return activeSource.getRecord();
    }

    @Override
    public PlayListComponent getCurrentTrack() {
        return activeSource.getCurrentTrack();
    }

    @Override
    public int getCurrentTimeTrack() {
        return activeSource.getCurrentTimeTrack();
    }

    @Override
    public void setCurrentTrack(PlayListComponent track) {
        activeSource.setCurrentTrack(track);
    }

    @Override
    public void setRecord(Record record) {
        activeSource.setRecord(record);
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(TrackObserver o) {
        activeSource.registerObserver(o);
    }

    public void removeObserver(TrackObserver o) {
        activeSource.removeObserver(o);
    }

    @Override
    public void registerObserver(StateObserver o) {
        activeSource.registerObserver(o);
    }

    public void removeObserver(StateObserver o) {
        activeSource.removeObserver(o);
    }

    @Override
    public void registerObserver(RecordObserver o) {
        activeSource.registerObserver(o);
    }

    public void removeObserver(RecordObserver o) {
        activeSource.removeObserver(o);
    }

    /**
     *
     */
    public void notifyMusicPlayerObservers() {
        if (musicPlayerObservers != null) {
            for (int i = 0; i < musicPlayerObservers.size(); i++) {
                MusicPlayerObserver observer = (MusicPlayerObserver) musicPlayerObservers.get(i);
                observer.updateMusicPlayer();
            }
        }
    }

    @Override
    public void registerObserver(MusicPlayerObserver o) {
        if (musicPlayerObservers.contains(o) == false) {
            musicPlayerObservers.add(o);
        }
    }

    public void removeObserver(MusicPlayerObserver o) {
        int i = musicPlayerObservers.indexOf(o);
        if (i >= 0) {
            musicPlayerObservers.remove(i);
        }
    }

    @Override
    public LinkedList<MusicPlayer> getSources() {
        return new LinkedList(sources);
    }

    @Override
    public MusicPlayer getSource(String title) {
        for (MusicPlayer ms : sources) {
            if (ms.getTitle().equals(title)) {
                return ms;
            }
        }
        return null;
    }

    @Override
    public boolean hasPlay() {
        return activeSource.hasPlay();
    }

    @Override
    public boolean hasStop() {
        return activeSource.hasStop();
    }

    @Override
    public boolean hasNext() {
        return activeSource.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return activeSource.hasPrevious();
    }

    @Override
    public boolean hasPause() {
        return activeSource.hasPause();
    }

    @Override
    public boolean hasTracks() {
        return activeSource.hasTracks();
    }

    @Override
    public boolean hasCurrentTime() {
        return activeSource.hasCurrentTime();
    }

    public void setMusicSystemState(MusicSystemState state) {
        activeSource.setMusicSystemState(state);
    }

    @Override
    public void registerObserver(TrackTimeObserver o) {
        activeSource.registerObserver(o);
    }

    public void removeObserver(TrackTimeObserver o) {
        activeSource.removeObserver(o);
    }

    /**
     * @return the port
     */
    @Override
    public ServerAddr getServerAddr() {
        return serverAddr;
    }

    @Override
    public void registerObserver(VolumeObserver o) {
        activeSource.registerObserver(o);
    }

    public void removeObserver(VolumeObserver o) {
        activeSource.removeObserver(o);
    }

    @Override
    public void registerObserver(ServerPoolObserver o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
