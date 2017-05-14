package de.beinlich.markus.musicsystem.model.net;

import de.beinlich.markus.musicsystem.model.TrackObserver;
import de.beinlich.markus.musicsystem.model.TrackTimeObserver;
import de.beinlich.markus.musicsystem.model.VolumeObserver;
import static de.beinlich.markus.musicsystem.model.net.ProtokollType.*;
//import de.beinlich.markus.musicsystem.gui.*;
import de.beinlich.markus.musicsystem.model.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

public class MusicClient implements Observer, MusicSystemInterfaceObserver, MusicSystemControllerInterface, MusicCollectionInterface {

//    private final MusicClientApp mca;
    private transient final ArrayList<MusicPlayerObserver> musicPlayerObservers = new ArrayList<>();
    private transient final ArrayList<VolumeObserver> volumeObservers = new ArrayList<>();
    private transient final ArrayList<TrackTimeObserver> trackTimeObservers = new ArrayList<>();
    private transient final ArrayList<TrackObserver> trackObservers = new ArrayList<>();
    private transient final ArrayList<StateObserver> stateObservers = new ArrayList<>();
    private transient final ArrayList<RecordObserver> recordObservers = new ArrayList<>();
    private transient final ArrayList<MusicCollectionObserver> musicCollectionObservers = new ArrayList<>();
    private transient final ArrayList<ServerPoolObserver> serverPoolObservers = new ArrayList<>();

    private final MusicClientNet musicClientNet;

    private ServerAddr currentServerAddr;
    private MusicSystemDto musicSystem;
    private RecordDto record;
    private final ServerPool serverPool;
    private MusicPlayerDto musicPlayer;
    private MusicCollectionDto musicCollection;
    private PlayListComponentDto playListComponent;
    private MusicSystemState musicSystemState;
    private double volume;
    private double oldVolume;
    private int trackTime;
    private ClientInit clientInit;
    private final String clientName;

    public MusicClient(String clientName) {

        this.clientName = clientName;
        this.musicClientNet = new MusicClientNet(clientName);
        this.serverPool = ServerPool.getInstance();
        this.currentServerAddr = null;
        this.musicCollection = new MusicCollectionDto();
        System.out.println("Alle:" + serverPool.toString());
        System.out.println("currentServerAddr: " + currentServerAddr);
        musicClientNet.addObserver(this);
        musicClientNet.netzwerkEinrichten();
        System.out.println(System.currentTimeMillis() + "netzwerk eingerichtet: ");
    }

    @Override
    public MusicPlayerInterface getActivePlayer() {
        return (MusicPlayerInterface) musicPlayer;
    }

    @Override
    public RecordInterface getRecord() {
        return (RecordInterface) record;
    }

    @Override
    public void setRecord(RecordInterface record) {
        if (!this.record.equals(record)) {
            try {
                musicClientNet.writeObject(new Protokoll(RECORD_SELECTED, record));

            } catch (InvalidObjectException ex) {
                Logger.getLogger(MusicClient.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public MusicSystemState getMusicSystemState() {
        return musicSystemState;
    }

    @Override
    public List<MusicPlayerInterface> getPlayers() {
        if (musicSystem == null) {
            return new ArrayList<>();
        }
        return musicSystem.players;
    }

    @Override
    public PlayListComponentInterface getCurrentTrack() {
        return (PlayListComponentInterface) playListComponent;
    }

    @Override
    public String getMusicSystemName() {
        if (musicSystem == null) {
            return null;
        }
        return musicSystem.musicSystemName;
    }

    @Override
    public String getLocation() {
        if (musicSystem == null) {
            return null;
        }
        return musicSystem.location;
    }

    @Override
    public int getCurrentTimeTrack() {
        return trackTime;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public void play() {
        try {
            musicClientNet.writeObject(new Protokoll(CLIENT_COMMAND_PLAY, musicSystemState));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void pause() {
        try {
            musicClientNet.writeObject(new Protokoll(CLIENT_COMMAND_PAUSE, musicSystemState));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void next() {
        try {
            musicClientNet.writeObject(new Protokoll(CLIENT_COMMAND_NEXT, playListComponent));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void previous() {
        try {
            musicClientNet.writeObject(new Protokoll(CLIENT_COMMAND_PREVIOUS, playListComponent));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() {
        try {
            musicClientNet.writeObject(new Protokoll(CLIENT_COMMAND_STOP, musicSystemState));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setVolume(double volume) {
        try {
            musicClientNet.writeObject(new Protokoll(VOLUME, new Double(volume)));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void seek(int currentTimeTrack) {
        try {
            musicClientNet.writeObject(new Protokoll(TRACK_TIME, currentTimeTrack));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setCurrentTrack(PlayListComponentInterface track) {
        try {
            //das Verändern des musicSystem/MusicSystem-Objektes muss vom Model/Server aus erfolgen. Sonst gibt es Rückkoppelungen
            //musicSystem.setCurrentTrack(listCurrentRecord.getSelectedValue());
            musicClientNet.writeObject(new Protokoll(TRACK_SELECTED, track));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setActivePlayer(MusicPlayerInterface activePlayer) throws IllegalePlayerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ServerAddr getServerAddr() {
        if (musicSystem == null) {
            return null;
        }
        return musicSystem.serverAddr;
    }

    @Override
    public boolean hasPause() {
        if (musicSystem == null || musicSystem.activePlayer == null) {
            return false;
        }
        return musicSystem.activePlayer.hasPause;
    }

    @Override
    public boolean hasPlay() {
        if (musicSystem == null || musicSystem.activePlayer == null) {
            return false;
        }
        return musicSystem.activePlayer.hasPlay;
    }

    @Override
    public boolean hasNext() {
        if (musicSystem == null || musicSystem.activePlayer == null) {
            return false;
        }
        return musicSystem.activePlayer.hasNext;
    }

    @Override
    public boolean hasPrevious() {
        if (musicSystem == null || musicSystem.activePlayer == null) {
            return false;
        }
        return musicSystem.activePlayer.hasPrevious;
    }

    @Override
    public boolean hasStop() {
        if (musicSystem == null || musicSystem.activePlayer == null) {
            return false;
        }
        return musicSystem.activePlayer.hasStop;
    }

    @Override
    public boolean hasTracks() {
        if (musicSystem == null || musicSystem.activePlayer == null) {
            return false;
        }
        return musicSystem.activePlayer.hasTracks;
    }

    @Override
    public boolean hasCurrentTime() {
        if (musicSystem == null || musicSystem.activePlayer == null) {
            return false;
        }
        return musicSystem.activePlayer.hasCurrentTime;
    }

    public void notifyStateObservers() {
//        System.out.println(System.currentTimeMillis() + "notifyStateObservers: " + stateObservers.size());
        if (stateObservers != null) {
            for (int i = 0; i < stateObservers.size(); i++) {
                StateObserver observer = (StateObserver) stateObservers.get(i);
                observer.updateState();
            }
        }
    }

    /**
     *
     */
    public void notifyTrackObservers() {
        if (trackObservers != null) {
            for (int i = 0; i < trackObservers.size(); i++) {
                TrackObserver observer = (TrackObserver) trackObservers.get(i);
                observer.updateTrack();
            }
        }
    }

    /**
     *
     */
    public void notifyTrackTimeObservers() {
        if (trackTimeObservers != null) {
            for (int i = 0; i < trackTimeObservers.size(); i++) {
                TrackTimeObserver observer = (TrackTimeObserver) trackTimeObservers.get(i);
                observer.updateTrackTime();
            }
        }
    }

    public void notifyVolumeObservers() {
        if (volumeObservers != null) {
            for (int i = 0; i < volumeObservers.size(); i++) {
                VolumeObserver observer = (VolumeObserver) volumeObservers.get(i);
                observer.updateVolume();
            }
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(TrackTimeObserver o) {
        if (trackTimeObservers.contains(o) == false) {
            trackTimeObservers.add(o);
        }
    }

    @Override
    public void registerObserver(VolumeObserver o) {
        if (volumeObservers.contains(o) == false) {
            volumeObservers.add(o);
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(TrackObserver o) {
        if (trackObservers.contains(o) == false) {
            trackObservers.add(o);
        }
    }

    @Override
    public void registerObserver(StateObserver o) {
        if (stateObservers.contains(o) == false) {
            stateObservers.add(o);
        }
    }

    public void notifyMusicPlayerObservers() {
        if (musicPlayerObservers != null) {
            for (int i = 0; i < musicPlayerObservers.size(); i++) {
                MusicPlayerObserver observer = (MusicPlayerObserver) musicPlayerObservers.get(i);
                observer.updateMusicPlayer();
            }
        }
    }

    public void notifyRecordObservers() {
        if (recordObservers != null) {
            System.out.println(System.currentTimeMillis() + "notifyRecordObservers:" + recordObservers.size());
            for (int i = 0; i < recordObservers.size(); i++) {
                RecordObserver observer = (RecordObserver) recordObservers.get(i);
                observer.updateRecord();
            }
        }
    }

    public void notifyMusicCollectionObservers() {
        if (musicCollectionObservers != null) {
            System.out.println(System.currentTimeMillis() + "notifyMusicCollectionObservers:" + musicCollectionObservers.size());
            for (int i = 0; i < musicCollectionObservers.size(); i++) {
                MusicCollectionObserver observer = (MusicCollectionObserver) musicCollectionObservers.get(i);
                observer.updateMusicCollection();
            }
        }
    }

    public void notifyServerPoolObservers() {
        if (serverPoolObservers != null) {
            System.out.println(System.currentTimeMillis() + "notifyServerPoolObservers:" + serverPoolObservers.size());
            for (int i = 0; i < serverPoolObservers.size(); i++) {
                ServerPoolObserver observer = (ServerPoolObserver) serverPoolObservers.get(i);
                observer.updateServerPool();
            }
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(RecordObserver o) {
        if (recordObservers.contains(o) == false) {
            recordObservers.add(o);
        }
    }

    /**
     *
     * @param o
     */
    @Override
    public void registerObserver(MusicPlayerObserver o) {
        if (musicPlayerObservers.contains(o) == false) {
            musicPlayerObservers.add(o);
        }
    }

    @Override
    public void registerObserver(MusicCollectionObserver o) {
        if (musicCollectionObservers.contains(o) == false) {
            musicCollectionObservers.add(o);
        }
    }

    @Override
    public void registerObserver(ServerPoolObserver o) {
        if (serverPoolObservers.contains(o) == false) {
            serverPoolObservers.add(o);
        }
    }

    @Override
    public void setActivePlayer(String selectedPlayer) {

        if (!(musicPlayer.title.equals(selectedPlayer))) {
            try {
                musicClientNet.writeObject(new Protokoll(MUSIC_PLAYER_SELECTED, selectedPlayer));

            } catch (InvalidObjectException ex) {
                Logger.getLogger(MusicClient.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public MusicPlayerInterface getPlayer(String title) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RecordInterface> getRecords() {
        if (musicCollection == null) {
            return new ArrayList<>();
        }
        return musicCollection.getRecords();
    }

    @Override
    public Record getRecord(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MusicSystemDto getDto() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MusicCollectionDto getMusicCollectionDto() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RecordInterface getRecordById(int rid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFormat(String format) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public void update(Observable o, Object arg) {

        Protokoll nachricht;
        MusicSystemState state;
        System.out.println("Observer: ");
        if (o instanceof MusicClientNet) {
            nachricht = (Protokoll) arg;
            System.out.println(System.currentTimeMillis() + "CLIENT: gelesen: " + nachricht + " - " + o.getClass());
            switch (nachricht.getProtokollType()) {
                case CLIENT_INIT:
                    clientInit = (ClientInit) nachricht.getValue();

                    musicSystem = clientInit.getMusicSystem();
                    currentServerAddr = musicSystem.serverAddr;
                    musicCollection = clientInit.getMusicCollection();
                    ServerPool.getInstance().addServers(clientInit.getServerPool().getServers());
                    musicSystemState = musicSystem.activePlayer.musicSystemState;
                    record = musicSystem.activePlayer.record;
                    musicPlayer = musicSystem.activePlayer;
                    volume = musicSystem.activePlayer.volume;
                    oldVolume = volume;
                    playListComponent = musicSystem.activePlayer.currentTrack;
                    trackTime = musicSystem.activePlayer.currentTimeTrack;

                    notifyRecordObservers();
                    notifyServerPoolObservers();
                    notifyMusicCollectionObservers();
                    notifyMusicPlayerObservers();
                    notifyTrackObservers();
                    notifyTrackTimeObservers();
                    notifyVolumeObservers();

                    break;
                case SERVER_DISCONNECT:
                    musicSystem = new MusicSystemDto();
                    currentServerAddr = null;
                    musicCollection = new MusicCollectionDto();
                    ServerPool.getInstance().clear();
                    musicSystemState = null;
                    record = new RecordDto();
                    musicPlayer = new MusicPlayerDto();
                    volume = 0.0;
                    oldVolume = 0.0;
                    playListComponent = new PlayListComponentDto();
                    trackTime = 0;

                    notifyRecordObservers();
                    notifyMusicCollectionObservers();
                    notifyMusicPlayerObservers();
                    notifyTrackObservers();
                    notifyTrackTimeObservers();
                    notifyVolumeObservers();
                    notifyServerPoolObservers();
                    break;
                case MUSIC_COLLECTION_DTO:
                    this.musicCollection = (MusicCollectionDto) nachricht.getValue();
                    notifyMusicCollectionObservers();
//                            mca.updateMusicCollection(musicClient.getMusicCollection());
                    break;
                case MUSIC_PLAYER_DTO:
                    if (!((MusicPlayerDto) nachricht.getValue()).equals(this.musicPlayer)) {
                        musicPlayer = (MusicPlayerDto) nachricht.getValue();
                        this.musicSystem.activePlayer = musicPlayer;
                        notifyMusicPlayerObservers();
//                                mca.updateMusicPlayer(musicPlayer);
                    }
                    break;
                case RECORD_DTO:
                    //Achtung: Rückkopplung vermeiden
                    if (!(((RecordDto) nachricht.getValue()).equals(this.record))) {
                        record = (RecordDto) nachricht.getValue();
                        System.out.println(System.currentTimeMillis() + "RECORD");
                        notifyRecordObservers();
                    }
                    break;
                case STATE:
                    //Achtung: Rückkopplung vermeiden
                    state = (MusicSystemState) nachricht.getValue();
                    System.out.println(System.currentTimeMillis() + "State");
                    musicSystemState = state;
                    break;
                case PLAY_LIST_COMPONENT_DTO:
                    //Achtung: Rückkopplung vermeiden
                    if (!(((PlayListComponentDto) nachricht.getValue()).equals(this.playListComponent))) {
                        playListComponent = (PlayListComponentDto) nachricht.getValue();
                        System.out.println(System.currentTimeMillis() + "TRACK");
                        this.trackTime = 0;
                        notifyTrackObservers();
                    }
                    break;
                case TRACK_TIME:
                    trackTime = (int) nachricht.getValue();
                    notifyTrackTimeObservers();
                    break;
                case VOLUME:
                    //Achtung: Rückkopplung vermeiden
                    System.out.println("VOLUME: gelesen: " + (double) nachricht.getValue() + " current: " + this.getVolume() + " old: " + this.getOldVolume());
                    if (((double) nachricht.getValue()) != this.getVolume()
                            && ((double) nachricht.getValue()) != this.getOldVolume()) {
                        this.setOldVolume(this.getVolume());
                        volume = (double) nachricht.getValue();
                        notifyVolumeObservers();
                    }
                    break;
                case SERVER_POOL:
                    this.serverPool.setServers((Map<String, ServerAddr>) nachricht.getValue());
                    notifyServerPoolObservers();
//                            mca.updateServerPool(musicClient.serverPool);
                    break;
                default:
                    System.out.println(System.currentTimeMillis() + "Unbekannte Nachricht:" + nachricht.getProtokollType());
            }
        }

    }

    public boolean switchToServer(String newServer) {
        return musicClientNet.switchToServer(newServer);
    }

    /**
     * @return the currentServerAddr
     */
    public ServerAddr getCurrentServerAddr() {
        return currentServerAddr;
    }

    /**
     * @param currentServerAddr the currentServerAddr to set
     */
    public void setCurrentServerAddr(ServerAddr currentServerAddr) {
        this.currentServerAddr = currentServerAddr;
    }

    /**
     * @return the oldVolume
     */
    public double getOldVolume() {
        return oldVolume;
    }

    /**
     * @param oldVolume the oldVolume to set
     */
    public void setOldVolume(double oldVolume) {
        this.oldVolume = oldVolume;
    }

    public MusicCollectionDto getMusicCollection() {
        return musicCollection;
    }

    public ServerPool getServerPool() {
        return serverPool;
    }

    public String getClientName() {
        return clientName;
    }

}
