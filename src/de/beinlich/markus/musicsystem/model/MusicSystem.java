 /*
 */
package de.beinlich.markus.musicsystem.model;

import de.beinlich.markus.musicsystem.model.net.ServerPoolObserver;
import de.beinlich.markus.musicsystem.model.net.ServerAddr;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.*;
import jdk.nashorn.api.scripting.*;

/**
 * Ein MusicSystem ist der zentrale Teil einer Musikanlage. Hier kann
 * beispielsweise die Lautstärke geregelt werden. Als Musik-Quelle dienen eine
 * oder mehrere MusicPlayer.
 *
 * Eine dieser MusicPlayer ist der activePlayer. Die Methoden zur Steuerung der
 * Musikwiedergabe (Play, Next, ...) beziehen sich auf die activePlayer.
 *
 * Die Klasse stellt mehrere Observable Verfügung: 1. MusicPlayerObserver: Es
 * werden alle Observer informiert, wenn sich die activePlayer ändert. 2.
 * RecordObserver: Es werden alle Observer informiert, wenn sich der aktuelle
 * Record ändert. 3. TrackObserver: Es werden alle Observer informiert, wenn
 * sich der aktuelle Track ändert. 4. TrackTimeObserver: Es werden alle Observer
 * informiert, wenn sich die aktuelle TrackTime ändert.
 *
 * @author Markus Beinlich
 */
class MusicSystem implements MusicSystemInterfaceObserver {

    private static MusicSystem uniqueInstance;
    private String musicSystemName;
    private boolean power;
    private boolean onOffSwitch;
    private String location;
    private AbstractMusicPlayer activePlayer;
    private ServerAddr serverAddr;
    final private LinkedList<AbstractMusicPlayer> players;

    private transient final ArrayList<MusicPlayerObserver> musicPlayerObservers = new ArrayList<>();

    private MusicSystem() {
        this("musicsystem");
    }

    private MusicSystem(String name) {
        this(name, "Sonstwo");
    }

    private MusicSystem(String name, String location) {
        this(name, location, false);
    }

    private MusicSystem(String name, String location, boolean power) {
        this(name, location, power, false);
    }

    private MusicSystem(String name, String location, boolean power, boolean onOffSwitch) {
        this.musicSystemName = name;
        this.location = name;
        this.power = power;
        this.onOffSwitch = onOffSwitch;
        this.players = new LinkedList<>();
    }

    public static synchronized MusicSystem getInstance(String name) {
        if (uniqueInstance == null) {
            SystemName.INSTANCE.setName(name);
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
     *
     * @param name
     */
    private void readConfiguration(String name) {
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
            this.setMusicSystemName((String) obj.getMember("name"));
        }
        if (obj.hasMember("location")) {
            this.setLocation((String) obj.getMember("location"));
        }
        if (obj.hasMember("port")) {
            port = Integer.parseInt((String) obj.getMember("port"));
        }
//        if (obj.hasMember("server_ip")) {
        //            server_ip = (String) obj.getMember("server_ip");
        try {
            server_ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            Logger.getLogger(MusicSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.serverAddr = new ServerAddr(port, server_ip, this.getMusicSystemName(), true);
        this.setPower(true);
        this.setOnOffSwitch(true);

        for (int m = 0; ms.hasSlot(m); m++) {
            JSObject msSl = (JSObject) ms.getSlot(m);
            if (msSl.hasMember("type")) {
                switch ((String) msSl.getMember("type")) {
                    case "CdPlayer":
                        CdPlayer cdPlayer = new CdPlayer();
                        cdPlayer.setTitle((msSl.hasMember("title")) ? (String) msSl.getMember("title") : "CD");
                        this.addPlayer(cdPlayer);
                        break;
                    case "Mp3Player":
                        Mp3Player mp3Player = new Mp3Player();
                        mp3Player.setTitle((msSl.hasMember("title")) ? (String) msSl.getMember("title") : "Mp3");
                        this.addPlayer(mp3Player);
                        break;
                    case "RecordPlayer":
                        RecordPlayer recordPlayer = new RecordPlayer();
                        recordPlayer.setTitle((msSl.hasMember("title")) ? (String) msSl.getMember("title") : "Plattenspieler");
                        this.addPlayer(recordPlayer);
                        break;
                    case "Radio":
                        Radio radio = new Radio();
                        radio.setTitle((msSl.hasMember("title")) ? (String) msSl.getMember("title") : "Radio");
                        this.addPlayer(radio);
                        break;
                    default:
                        System.out.println(System.currentTimeMillis() + "Unbekannter type" + (String) msSl.getMember("type"));
                }
            }
        }
        try {
            this.setActivePlayer(this.getPlayers().getFirst());
        } catch (IllegalePlayerException ex) {
            System.out.println(System.currentTimeMillis() + "Gerät kann nicht verwendet werden. Fehler: " + ex.getMessage());
        }

    }

    public MusicSystemDto getDto() {
        MusicSystemDto musicSystemDto = new MusicSystemDto();
        musicSystemDto.musicSystemName = this.musicSystemName;
        musicSystemDto.power = this.power;
        musicSystemDto.onOffSwitch = this.onOffSwitch;
        musicSystemDto.location = this.location;
        musicSystemDto.activePlayer = this.activePlayer.getDto();
        musicSystemDto.serverAddr = this.serverAddr;
        musicSystemDto.players = new LinkedList();
        for (MusicPlayerInterface mp : players) {
            musicSystemDto.players.add(((AbstractMusicPlayer) mp).getDto());
        }
        return musicSystemDto;
    }

    /**
     *
     * @return
     */
    @Override
    public double getVolume() {
        return activePlayer.getVolume();
    }

    /**
     * @param volume the volume to set
     */
    @Override
    public void setVolume(double volume) {
        if (volume > 100) {
            System.out.println(System.currentTimeMillis() + "Die maximale Lautstärke ist 100.");
            activePlayer.setVolume(100.0);
        } else if (volume < 0) {
            System.out.println(System.currentTimeMillis() + "Die minimal Lautstärke ist 0.");
            activePlayer.setVolume(0.0);
        } else {
            activePlayer.setVolume(volume);
        }
        System.out.println(System.currentTimeMillis() + "Lautstärke: " + getVolume());
    }

    @Override
    public void play() {
        boolean ok = true;
        if (activePlayer == null) {
            System.out.println(System.currentTimeMillis() + "Sie müssen erst mit 'setActivePlayer(???) eine Music-Quelle angeben.");
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
            activePlayer.play();
        }
    }

    @Override
    public void pause() {
        activePlayer.pause();
    }

    @Override
    public void stop() {
        activePlayer.stop();
    }

    @Override
    public void previous() {
        activePlayer.previous();
    }

    @Override
    public void next() {
        activePlayer.next();
    }

    @Override
    public MusicPlayerInterface getActivePlayer() {
        return (MusicPlayerInterface) activePlayer;
    }

    @Override
    public void setActivePlayer(MusicPlayerInterface activePlayer) throws IllegalePlayerException {
        //prüfen, ob der zu aktivierende Player zu den vorhandenen Playern gehört
        if (-1 == getPlayers().indexOf(activePlayer)) {
            throw new IllegalePlayerException("Player " + activePlayer + " is not part of Players: " + getPlayers());
        }
        //prüfen, ob es bereits einen aktiven Player gibt und diese gegebenfalls stoppen
        if (this.activePlayer != null) {
            this.activePlayer.stop();
        }
        this.activePlayer = (AbstractMusicPlayer) activePlayer;

        notifyMusicPlayerObservers();
//        this.activePlayer.notifyRecordObservers();
//        this.activePlayer.notifyTrackObservers();
//        this.activePlayer.notifyStateObservers();
    }

    private void addPlayer(AbstractMusicPlayer player) {
        players.add(player);
        notifyMusicPlayerObservers();
    }

    @Override
    public String toString() {
        String as = (this.getActivePlayer() == null) ? "" : "\nAktive Quelle:" + this.getActivePlayer().getClass().getSimpleName();
        return "\nGerätename: " + this.getMusicSystemName()
                + "\nStandort: " + this.getLocation()
                + "\nStrom vorhanden: " + isPower()
                + "\nGerät eingeschaltet:" + isOnOffSwitch()
                + as
                + "\nQuellen: " + this.getPlayers().toString();
    }

    @Override
    public MusicSystemState getMusicSystemState() {
        return activePlayer.getMusicSystemState();
    }

    @Override
    public Record getRecord() {
        return activePlayer.getRecord();
    }

    @Override
    public PlayListComponentInterface getCurrentTrack() {
        return (PlayListComponentInterface) activePlayer.getCurrentTrack();
    }

    @Override
    public int getCurrentTimeTrack() {
        return activePlayer.getCurrentTimeTrack();
    }

    @Override
    public void setCurrentTrack(PlayListComponentInterface track) {
        activePlayer.setCurrentTrack((PlayListComponent) track);
    }
    
    @Override
    public void seek(int currentTimeTrack) {
        activePlayer.seek(currentTimeTrack);
    }

    @Override
    public void setRecord(RecordInterface record) {
        activePlayer.setRecord((Record)record);
    }

    @Override
    public void registerObserver(TrackObserver o) {
        activePlayer.registerObserver(o);
    }

    public void removeObserver(TrackObserver o) {
        activePlayer.removeObserver(o);
    }

    @Override
    public void registerObserver(StateObserver o) {
        activePlayer.registerObserver(o);
    }

    public void removeObserver(StateObserver o) {
        activePlayer.removeObserver(o);
    }

    @Override
    public void registerObserver(RecordObserver o) {
        activePlayer.registerObserver(o);
    }

    public void removeObserver(RecordObserver o) {
        activePlayer.removeObserver(o);
    }

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
    public LinkedList<MusicPlayerInterface> getPlayers() {
        return new LinkedList(players);
    }

    @Override
    public MusicPlayerInterface getPlayer(String title) {
        for (MusicPlayerInterface mp : players) {
            if (mp.getTitle().equals(title)) {
                return mp;
            }
        }
        return null;
    }

    @Override
    public boolean hasPlay() {
        return activePlayer.hasPlay();
    }

    @Override
    public boolean hasStop() {
        return activePlayer.hasStop();
    }

    @Override
    public boolean hasNext() {
        return activePlayer.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return activePlayer.hasPrevious();
    }

    @Override
    public boolean hasPause() {
        return activePlayer.hasPause();
    }

    @Override
    public boolean hasTracks() {
        return activePlayer.hasTracks();
    }

    @Override
    public boolean hasCurrentTime() {
        return activePlayer.hasCurrentTime();
    }

    public void setMusicSystemState(MusicSystemState state) {
        activePlayer.setMusicSystemState(state);
    }

    @Override
    public void registerObserver(TrackTimeObserver o) {
        activePlayer.registerObserver(o);
    }

    public void removeObserver(TrackTimeObserver o) {
        activePlayer.removeObserver(o);
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
        activePlayer.registerObserver(o);
    }

    public void removeObserver(VolumeObserver o) {
        activePlayer.removeObserver(o);
    }

    @Override
    public void registerObserver(ServerPoolObserver o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMusicSystemName() {
        return musicSystemName;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the onOffSwitch
     */
    public boolean isOnOffSwitch() {
        return onOffSwitch;
    }

    /**
     * @param onOffSwitch the onOffSwitch to set
     */
    public void setOnOffSwitch(boolean onOffSwitch) {
        if (onOffSwitch == true & this.isPower() == false) {
            System.out.println(System.currentTimeMillis() + "Bitte erst für Strom sorgen. Gerät kann nicht eingeschaltet werden.");
        } else {
            this.onOffSwitch = onOffSwitch;
            System.out.println(System.currentTimeMillis() + this.getMusicSystemName() + " Gerät ist " + (power ? "an." : "aus."));
        }
    }

    /**
     * @return the power
     */
    public boolean isPower() {
        return power;
    }

    /**
     * @param power the power to set
     */
    public void setPower(boolean power) {
        this.power = power;
        System.out.println(System.currentTimeMillis() + this.getMusicSystemName() + " Strom ist " + (power ? "an." : "aus."));
    }

    /**
     * @param musicSystemName the name to set
     */
    public void setMusicSystemName(String musicSystemName) {
        this.musicSystemName = musicSystemName;
    }
}
