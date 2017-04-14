/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.model.net;

import de.beinlich.markus.musicsystem.model.ServerPool;
import de.beinlich.markus.musicsystem.model.ServerAddr;
//import de.beinlich.markus.musicsystem.gui.*;
import de.beinlich.markus.musicsystem.model.*;
import static de.beinlich.markus.musicsystem.model.net.ProtokollType.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.SwingWorker;

/**
 *
 * @author Markus Beinlich
 */
public class MusicClient extends SwingWorker<Void, Void> implements MusicSystemInterface, MusicSystemControllerInterface, MusicCollectionInterface {

//    private final MusicClientApp mca;
    private transient final ArrayList<MusicPlayerObserver> musicPlayerObservers = new ArrayList<>();
    private transient final ArrayList<VolumeObserver> volumeObservers = new ArrayList<>();
    private transient final ArrayList<TrackTimeObserver> trackTimeObservers = new ArrayList<>();
    private transient final ArrayList<TrackObserver> trackObservers = new ArrayList<>();
    private transient final ArrayList<StateObserver> stateObservers = new ArrayList<>();
    private transient final ArrayList<RecordObserver> recordObservers = new ArrayList<>();
    private transient final ArrayList<MusicCollectionObserver> musicCollectionObservers = new ArrayList<>();
    private transient final ArrayList<ServerPoolObserver> serverPoolObservers = new ArrayList<>();

    // Verbindungsaufbau mit dem Server
    public Socket socket;
    private Socket newSocket;
    private ServerAddr currentServerAddr;
    private Thread readerThread;
    //
    // IO-Klassen zur Kommunikation
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private static final int MAX_RECONNECTIONS = 10;
    private int reconnections = 0;

    private MusicSystemDto musicSystem;
    private RecordDto record;
    private ServerPool serverPool;
    private MusicPlayerDto musicPlayer;
    private MusicCollectionDto musicCollection;
    private PlayListComponentDto playListComponent;
    private MusicSystemState musicSystemState;
    private double volume;
    private double oldVolume;
    private int trackTime;
    private ClientInit clientInit;
    private static String clientName;

    public MusicClient(String clientName) {
//        this.mca = mca;
        this.clientName = clientName;
        serverPool = ServerPool.getInstance(clientName);
        currentServerAddr = serverPool.getFirstServer();
        System.out.println("Alle:" + serverPool.toString());
        System.out.println("currentServerAddr: " + currentServerAddr);
        netzwerkEinrichten(currentServerAddr);
        musicSystemObjectRead();
        startReaderThread();
        System.out.println(System.currentTimeMillis() + "netzwerk eingerichtet: ");
    }

    private void netzwerkEinrichten(ServerAddr serverAddr) {

        try {
            // Erzeugung eines Socket-Objekts
            //                  Rechner (Adresse / Name)
            //                  |            Port

            //Verbindungs-Parameter in property-file auslagern
            NetProperties netProperties = new NetProperties();
            System.out.println(System.currentTimeMillis() + "new Socket with " + serverAddr.getServer_ip() + serverAddr.getPort());
            socket = new Socket(serverAddr.getServer_ip(), serverAddr.getPort());
            System.out.println(System.currentTimeMillis() + "socket.connect");
            //socket.connect(socket.getRemoteSocketAddress() , 0);
            // Erzeugung der Kommunikations-Objekte
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (ConnectException e) {
            System.out.println(System.currentTimeMillis() + "Error while connecting. " + e.getMessage());
            this.tryToReconnect();
        } catch (SocketTimeoutException e) {
            System.out.println(System.currentTimeMillis() + "Connection: " + e.getMessage() + ".");
            this.tryToReconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startReaderThread() {
        // Thread der sich um die eingehende Kommunikation kümmert
        readerThread = new Thread(new MusicClient.EingehendReader(this));
        // Thread als (Hintergrund-) Service
        readerThread.setDaemon(true);
        readerThread.start();
        System.out.println(System.currentTimeMillis() + "CLIENT: Netzwerkverbindung steht jetzt");
    }

    private void musicSystemObjectRead() {
        Protokoll nachricht;
        ClientInit clientInit;

        try {
            // Als erstes write den Namen des eigenen Client übergeben!
            oos.writeObject(new Protokoll(ProtokollType.CLIENT_NAME, clientName));
            oos.flush();
            try {
                // reinkommende Nachrichten vom Server. Auf diese muss gewartet werden, 
                // da ansonsten die initialisierung der GUI nicht funktioniert.
                nachricht = (Protokoll) ois.readObject(); // blockiert!
                clientInit = (ClientInit) nachricht.getValue();
//                mca.setClientInit(clientInit);
                musicSystem = clientInit.getMusicSystem();
                musicCollection = clientInit.getMusicCollection();
                ServerPool.getInstance(clientName).addServers(clientInit.getServerPool());
                musicSystemState = musicSystem.activePlayer.musicSystemState;
                record = musicSystem.activePlayer.record;
                musicPlayer = musicSystem.activePlayer;
                volume = musicSystem.activePlayer.volume;
                oldVolume = volume;
                playListComponent = musicSystem.activePlayer.currentTrack;
                trackTime = musicSystem.activePlayer.currentTimeTrack;

            } catch (ClassNotFoundException ex) {
                System.out.println(ex);
            }

        } catch (IOException ex) {
            System.out.println(System.currentTimeMillis() + "no connection - " + ex);
        }
    }

    public void writeObject(Protokoll protokoll) {
        try {
            System.out.println(System.currentTimeMillis() + "writeObject:" + protokoll.getProtokollType() + ": " + protokoll.getValue());
            // einen Befehl an der Server übertragen
            oos.writeObject(protokoll);
            oos.flush();
        } catch (IOException ex) {
            Logger.getLogger(Protokoll.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void writeCommand(MusicClientCommand mCC) {
//        System.out.println(System.currentTimeMillis() + "writeCommand:" + mCC.getType() + " new:" + mCC.getNewState() + " old: " + mCC.getOldState());
//        try {
//            // einen Befehl an der Server übertragen
//            oos.writeObject(new Protokoll(ProtokollType.CLIENT_COMMAND, mCC));
//            oos.flush();
//        } catch (IOException ex) {
//            Logger.getLogger(MusicClientCommand.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }
    @Override
    protected Void doInBackground() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void tryToReconnect() {
//        this.disconnect();

        System.out.println(System.currentTimeMillis() + "I will try to reconnect in 10 seconds... (" + this.reconnections + "/10)");
        try {
            Thread.sleep(10000); //milliseconds
        } catch (InterruptedException e) {
        }

        if (this.reconnections < MAX_RECONNECTIONS) {
            this.reconnections++;
            this.netzwerkEinrichten(getCurrentServerAddr());

        } else {
            System.out.println(System.currentTimeMillis() + "Reconnection failed, exceeded max reconnection tries. Shutting down.");
//            this.disconnect();
            System.exit(0);
            return;
        }

    }

    public boolean switchToServer(String newServer) {
        ServerAddr serverAddr;
        System.out.println("switchToServer:" + newServer);
        serverAddr = serverPool.getServers().get(newServer);
        System.out.println("switchToServer:" + serverAddr + ServerPool.getInstance(clientName));

        try {
            //wenn es geklappt hat, kann die Verbindung zum alten Server getrennt werden
            System.out.println("Old Socket:" + socket.hashCode());
            newSocket = new Socket(serverAddr.getServer_ip(), serverAddr.getPort());
            socket.close();
            System.out.println("Old Socket:" + socket.hashCode());
            System.out.println("LocalSocketAddress: " + newSocket.getLocalSocketAddress());
            System.out.println("RemoteSocketAddress: " + newSocket.getRemoteSocketAddress());

            // Erzeugung der Kommunikations-Objekte
            ois = new ObjectInputStream(newSocket.getInputStream());
            oos = new ObjectOutputStream(newSocket.getOutputStream());
            socket = newSocket;
            this.currentServerAddr = serverAddr;
            musicSystemObjectRead();
            startReaderThread();
            System.out.println(System.currentTimeMillis() + "netzwerk eingerichtet: ");

        } catch (IOException ex) {
            Logger.getLogger(MusicClient.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public MusicPlayerInterface getActivePlayer() {
        return (MusicPlayerInterface)musicPlayer;
    }

    @Override
    public RecordInterface getRecord() {
        return (RecordInterface)record;
    }

    @Override
    public void setRecord(RecordInterface record) {
        if (!this.record.equals(record)) {
            try {
                writeObject(new Protokoll(RECORD_SELECTED, record));
            } catch (InvalidObjectException ex) {
                Logger.getLogger(MusicClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public MusicSystemState getMusicSystemState() {
        return musicSystemState;
    }

    @Override
    public List<MusicPlayerInterface> getPlayers() {
        List<MusicPlayerInterface> players = new LinkedList<>();
        for(MusicPlayerDto player: musicSystem.players) {
            players.add((MusicPlayerInterface)player);
        }
        return players;
    }

    @Override
    public PlayListComponentInterface getCurrentTrack() {
        return (PlayListComponentInterface)playListComponent;
    }

    @Override
    public String getMusicSystemName() {
        return musicSystem.musicSystemName;
    }

    @Override
    public String getLocation() {
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
            writeObject(new Protokoll(CLIENT_COMMAND_PLAY, musicSystemState));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void pause() {
        try {
            writeObject(new Protokoll(CLIENT_COMMAND_PAUSE, musicSystemState));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void next() {
        try {
            writeObject(new Protokoll(CLIENT_COMMAND_NEXT, playListComponent));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void previous() {
        try {
            writeObject(new Protokoll(CLIENT_COMMAND_PREVIOUS, playListComponent));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() {
        try {
            writeObject(new Protokoll(CLIENT_COMMAND_STOP, musicSystemState));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setVolume(double volume) {
        try {
            writeObject(new Protokoll(VOLUME, new Double(volume)));

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
            writeObject(new Protokoll(TRACK_SELECTED, track));

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasPause() {
        return musicSystem.activePlayer.hasPause;
    }

    @Override
    public boolean hasPlay() {
        return musicSystem.activePlayer.hasPlay;
    }

    @Override
    public boolean hasNext() {
        return musicSystem.activePlayer.hasNext;
    }

    @Override
    public boolean hasPrevious() {
        return musicSystem.activePlayer.hasPrevious;
    }

    @Override
    public boolean hasStop() {
        return musicSystem.activePlayer.hasStop;
    }

    @Override
    public boolean hasTracks() {
        return musicSystem.activePlayer.hasTracks;
    }

    @Override
    public boolean hasCurrentTime() {
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
                writeObject(new Protokoll(MUSIC_PLAYER_SELECTED, selectedPlayer));
            } catch (InvalidObjectException ex) {
                Logger.getLogger(MusicClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public MusicPlayerInterface getPlayer(String title) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RecordInterface> getRecords() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addRecord(Record record) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public void setRecord(Record record) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RecordInterface getRecordById(int rid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 

    class EingehendReader implements Runnable {

        private final MusicClient musicClient;

        private EingehendReader(MusicClient musicClient) {
            this.musicClient = musicClient;
        }

        @Override
        public void run() {
            Protokoll nachricht;
            MusicPlayerDto musicPlayer;
            RecordDto record;
            MusicSystemState state;
            PlayListComponentDto playListComponent;
            double volume;

            try {
                while (true) {

                    // reinkommende Nachrichten vom Server
                    Object o = ois.readObject();
                    nachricht = (Protokoll) o; // blockiert!
                    System.out.println(System.currentTimeMillis() + "CLIENT: gelesen: " + nachricht + " - " + o.getClass());
                    switch (nachricht.getProtokollType()) {
                        case MUSIC_COLLECTION_DTO:
                            musicClient.musicCollection = (MusicCollectionDto) nachricht.getValue();
                            notifyMusicCollectionObservers();
//                            mca.updateMusicCollection(musicClient.getMusicCollection());
                            break;
                        case MUSIC_PLAYER_DTO:
                            musicPlayer = (MusicPlayerDto) nachricht.getValue();
                            if (!musicPlayer.equals(musicClient.musicPlayer)) {
                                musicClient.musicSystem.activePlayer = musicPlayer;
                                musicClient.musicPlayer = musicPlayer;
                                notifyMusicPlayerObservers();
//                                mca.updateMusicPlayer(musicPlayer);
                            }
                            break;
                        case RECORD_DTO:
                            //Achtung: Rückkopplung vermeiden
                            record = (RecordDto) nachricht.getValue();
                            if (!(record.equals(musicClient.record))) {
                                System.out.println(System.currentTimeMillis() + "RECORD");
                                musicClient.record = record;
                                notifyRecordObservers();
//                                mca.updateRecord(record);
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
                            playListComponent = (PlayListComponentDto) nachricht.getValue();
                            if (!(playListComponent.equals(musicClient.playListComponent))) {
                                System.out.println(System.currentTimeMillis() + "TRACK");
                                musicClient.playListComponent = playListComponent;
                                notifyTrackObservers();
//                                mca.updatePlayListComponent(playListComponent);
                            }
                            break;
                        case TRACK_TIME:
                            trackTime = (int) nachricht.getValue();
                            notifyTrackTimeObservers();
//                            mca.updateTrackTime(trackTime);
                            break;
                        case VOLUME:
                            //Achtung: Rückkopplung vermeiden
                            volume = (double) nachricht.getValue();
                            if (volume != musicClient.getVolume() && volume != musicClient.getOldVolume()) {
                                musicClient.setOldVolume(musicClient.getVolume());
                                musicClient.volume = volume;
                                notifyVolumeObservers();
                            }
                            break;
                        case SERVER_POOL:
                            musicClient.serverPool = (ServerPool) nachricht.getValue();
                            notifyServerPoolObservers();
//                            mca.updateServerPool(musicClient.serverPool);
                            break;
                        default:
                            System.out.println(System.currentTimeMillis() + "Unbekannte Nachricht:" + nachricht.getProtokollType());
                    }
                }

            } catch (Exception ex) {
                System.out.println(System.currentTimeMillis() + "CLIENT: Verbindung zum Server beendet - " + ex);
                ex.printStackTrace();
            }
        }
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

    /**
     * @return the musicCollection
     */
    public MusicCollectionDto getMusicCollection() {
        return musicCollection;
    }

    /**
     * @return the serverPool
     */
    public ServerPool getServerPool() {
        return serverPool;
    }

    /**
     * @return the clientName
     */
    public static String getClientName() {
        return clientName;
    }

}
