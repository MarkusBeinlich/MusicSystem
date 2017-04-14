package de.beinlich.markus.musicsystem.model.net;

import de.beinlich.markus.musicsystem.model.*;
import static de.beinlich.markus.musicsystem.model.net.ProtokollType.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.SwingWorker;

// -OK- - binärer datenaustausch
// TODO - Protokoll optimierung
// TODO - RPC / RMI
public class MusicServer extends SwingWorker<Void, Void> implements VolumeObserver, TrackTimeObserver, TrackObserver, StateObserver, RecordObserver, MusicPlayerObserver {

    private final List<ObjectOutputStream> clients;
    private final List<ObjectOutputStream> servers;

    private MusicSystemInterface musicSystem; //Model
    private MusicSystemControllerInterface musicSystemController;
    private MusicCollectionInterface musicCollection;
    private ServerPool serverPool;
    private String name;

    public MusicServer(MusicSystemControllerInterface musicSystemController, MusicSystem musicSystem) {
//        this.msa = msa;
        clients = new ArrayList<>();
        servers = new ArrayList<>();
        this.musicSystem = musicSystem;
        this.musicSystemController = musicSystemController;
        //Den Namen des MusicServer auf den gleichen wert wie beim MusicSystem setzen.
        //Der Name wird für den Dateinamen des ServerPool verwendet.
        name = musicSystem.getMusicSystemName();
        musicCollection = MusicCollection.getInstance(musicSystem.getActivePlayer().getClass().getSimpleName());
        musicSystem.setRecord((Record) musicCollection.getRecord());
        serverPool = ServerPool.getInstance(name);

        musicSystem.registerObserver((VolumeObserver) this);
        musicSystem.registerObserver((TrackTimeObserver) this);
        musicSystem.registerObserver((TrackObserver) this);
        musicSystem.registerObserver((StateObserver) this);
        musicSystem.registerObserver((RecordObserver) this);
        musicSystem.registerObserver((MusicPlayerObserver) this);

    }

    @Override
    public Void doInBackground() {

        try {
            // windows> netstat -a

            // Server registriert sich unter der Port-Nr. 50000
            // der port kann auch über die JSON Konfigurationsdatei gesetzt werden
            // wenn dies der Fall ist, steht er im MusicSystem.serverAddr.port zur Verfügung
            ServerSocket serverSocket;
            if (musicSystem.getServerAddr().getPort() != 0) {
                serverSocket = new ServerSocket(musicSystem.getServerAddr().getPort());
                System.out.println("Port1: " + musicSystem.getServerAddr().getPort());
            } else {
                NetProperties netProperties = new NetProperties();
                serverSocket = new ServerSocket(Integer.parseInt(netProperties.getProperty("net.port")));
                System.out.println("Port2: " + netProperties.getProperty("net.port"));
            }
            //Alle bekannten Server abklappern, ob sie aktiv sind
            Socket socket;
            for (String server : getServerPool().getServers().keySet()) {
                ServerAddr sa = getServerPool().getServers().get(server);
                //kein Verbindungsversuch mit sich selbst.
                if (sa.equals(musicSystem.getServerAddr())) {
                    continue;
                }
                try {
                    // Erzeugung eines Socket-Objekts
                    //                  Rechner (Adresse / Name)
                    //                  |            Port

                    System.out.println(System.currentTimeMillis() + "Suche:" + sa);
                    socket = new Socket(sa.getServer_ip(), sa.getPort());  // "loca lhost" // "www.google.com"
                    System.out.println(System.currentTimeMillis() + "socket.connect");
                    new Thread(new ClientHandler(socket, this, true)).start();
                } catch (ConnectException e) {
                    System.out.println(System.currentTimeMillis() + "Error while connecting. " + e.getMessage());
                } catch (SocketTimeoutException e) {
                    System.out.println(System.currentTimeMillis() + "Connection: " + e.getMessage() + ".");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            while (true) {
                System.out.println(System.currentTimeMillis() + "Server lauscht!");

                // waiting for a client to connect
                socket = serverSocket.accept();  // blockiert!

                new Thread(new ClientHandler(socket, this, false)).start();

            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }

    public void done() {
        System.out.println(System.currentTimeMillis() + "MusicServer done");
    }

    private synchronized void talkToAll(Protokoll nachricht) {
        for (ObjectOutputStream oos : clients) {
            try {
                oos.writeObject(nachricht);
                oos.flush();
                System.out.println(System.currentTimeMillis() + "server: geschrieben: " + nachricht + "-" + System.currentTimeMillis());
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    private synchronized void talkToAllServer(Protokoll nachricht) {
        for (ObjectOutputStream oos : servers) {
            try {
                oos.writeObject(nachricht);
                oos.flush();
                System.out.println(System.currentTimeMillis() + "server: geschrieben: " + nachricht + "-" + System.currentTimeMillis());
            } catch (IOException ex) {
                System.out.println(ex);

            }
        }
    }

    @Override
    public void updateVolume() {
        System.out.println(System.currentTimeMillis() + "Server - updateVolume");
        try {
            talkToAll(new Protokoll(VOLUME, musicSystem.getVolume()));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public class ClientHandler implements Runnable {

        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private String name;
        private MusicServer musicServer;
        private MusicSystemInterface musicSystem;
        private MusicSystemControllerInterface musicSystemController;

        public ClientHandler(Socket socket, MusicServer musicServer, boolean isServer) {
            this.musicServer = musicServer;
            this.musicSystem = musicServer.musicSystem;
            this.musicSystemController = musicServer.musicSystemController;
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                ois = new ObjectInputStream(socket.getInputStream());
//                clients.add(oos);
                if (isServer) {
                    servers.add(oos);
                    oos.writeObject(new Protokoll(SERVER_ADDR,
                            musicServer.musicSystem.getServerAddr()));
                    oos.flush();
                }
            } catch (Exception ex) {
            }
        }

        @Override
        public void run() {
            Object o;
            Protokoll protokoll;
            ServerAddr serverAddr;
            ServerPool serverPool;
            RecordInterface record;
            String musicPlayerTitle;
            MusicPlayerInterface musicPlayer;
            PlayListComponentInterface playListComponent;

            try {
                // Liest als erste Zeile den Namen des Client bzw des Servers
                o = ois.readObject();
                protokoll = (Protokoll) o;
                switch (protokoll.getProtokollType()) {
                    case SERVER_ADDR:
                        servers.add(oos);
                        serverAddr = (ServerAddr) protokoll.getValue();
                        System.out.println(System.currentTimeMillis() + "SERVER: habe eine Verbindung mit " + serverAddr.getName());
                        ServerPool.getInstance(musicServer.getName()).addServer(serverAddr.getName(), serverAddr);
                        //Information über aktiven Server an alle aktiven Server weitergeben.
                        //Anderen Protokolltype verwenden, damit keine Endlosschleife entsteht.
                        talkToAllServer(new Protokoll(SERVER_POOL, ServerPool.getInstance(musicServer.getName())));
                        // Clients auch noch über den aktuellen Serverpool informieren
                        talkToAll(new Protokoll(SERVER_POOL, ServerPool.getInstance(musicServer.getName())));
                        break;
                    case SERVER_POOL:
                        serverPool = (ServerPool) protokoll.getValue();
                        ServerPool.getInstance(musicServer.getName()).addServers(serverPool);
                        break;
                    case CLIENT_NAME:
                        clients.add(oos);
                        name = (String) protokoll.getValue();
                        System.out.println(System.currentTimeMillis() + "SERVER: habe eine Verbindung mit " + name);
                        //Dem Client zur Initialisierung das init-Object mit hifi-Objekt, MusicCollection und ServerPool schicken 
                        try {
                            System.out.println(System.currentTimeMillis() + "MusicSystem in run " + musicSystem);
                            protokoll = new Protokoll(CLIENT_INIT,
                                    new ClientInit(musicSystem.getDto(),
                                            musicServer.musicCollection.getMusicCollectionDto(),
                                            musicServer.getServerPool()));
                            oos.writeObject(protokoll);
                            oos.flush();
//                    protokoll = new Protokoll(MUSIC_COLLECTION, CdCollection.getInstance());
//                    oos.writeObject(protokoll);
//                    oos.flush();
                        } catch (IOException ex) {
                            System.out.println(ex);
                        }
                        break;
                    default:
                        System.out.println(System.currentTimeMillis() + "Unbekannte Nachricht:" + protokoll.getProtokollType());
                        throw new NoSuchElementException("Unbekannte Nachricht:" + protokoll.getProtokollType());
                }

                System.out.println(System.currentTimeMillis() + "Warten auf Nachrichten");
                while (true) {

                    // warten auf client-nachrichten
                    o = ois.readObject();  // blockiert!
                    System.out.println(System.currentTimeMillis() + "Server gelesen Object:" + o);
                    protokoll = (Protokoll) o;
                    switch (protokoll.getProtokollType()) {
                        case SERVER_POOL:
                            serverPool = (ServerPool) protokoll.getValue();
                            ServerPool.getInstance(musicServer.getName()).addServers(serverPool);
                            break;
                        case MUSIC_PLAYER_SELECTED:
                            //Achtung: Rückkopplung vermeiden
                            musicPlayerTitle = (String) protokoll.getValue();
                            musicPlayer = musicSystem.getPlayer(musicPlayerTitle);
                            if (!(musicSystem.getActivePlayer().equals((musicPlayer)))) {
                                musicSystemController.setActivePlayer(musicPlayer.getTitle());
                            }
                            break;
                        case RECORD_SELECTED:
                            //Achtung: Rückkopplung vermeiden
                            record = (RecordInterface) protokoll.getValue();
                            if (!(musicSystem.getRecord().equals((record)))) {
                                musicSystemController.setRecord(record);
                            }
                            break;
                        case TRACK_SELECTED:
                            playListComponent = (PlayListComponentInterface) protokoll.getValue();
                            if (!(musicSystem.getCurrentTrack().equals(playListComponent))) {
                                musicSystemController.setCurrentTrack(playListComponent);
                            }
                            break;
                        case VOLUME:
                            musicSystemController.setVolume((double) protokoll.getValue());
                            System.out.println(System.currentTimeMillis() + "VOLUME done");
                            break;
                        case CLIENT_COMMAND_PLAY:
                            if (musicSystem.getMusicSystemState() == (MusicSystemState) protokoll.getValue()) {
                                musicSystemController.play();
                            }
                            break;
                        case CLIENT_COMMAND_NEXT:
                            if (((PlayListComponentInterface) protokoll.getValue()).getUid() == musicSystem.getCurrentTrack().getUid()) {
                                musicSystemController.next();
                                System.out.println(System.currentTimeMillis() + "Next done");
                            }
                            break;
                        case CLIENT_COMMAND_PREVIOUS:
                            if (((PlayListComponentInterface) protokoll.getValue()).getUid() == musicSystem.getCurrentTrack().getUid()) {
                                musicSystemController.previous();
                                System.out.println(System.currentTimeMillis() + "PREVIOUS done");
                            }
                            break;
                        case CLIENT_COMMAND_PAUSE:
                            if ((MusicSystemState) protokoll.getValue() == musicSystem.getMusicSystemState()) {
                                musicSystemController.pause();
                                System.out.println(System.currentTimeMillis() + "PAUSE done");
                            }
                            break;
                        case CLIENT_COMMAND_STOP:
                            if ((MusicSystemState) protokoll.getValue() == musicSystem.getMusicSystemState()) {
                                musicSystemController.stop();
                                System.out.println(System.currentTimeMillis() + "STOP done");
                            }
                            break;
                        default:
                            System.out.println(System.currentTimeMillis() + "Unbekannte Nachricht:" + protokoll.getProtokollType());
                            throw new NoSuchElementException("Unbekannte Nachricht:" + protokoll.getProtokollType());
                    }
                }
            } catch (ClassNotFoundException | IOException ex) {
                clients.remove(oos);
                System.out.println(System.currentTimeMillis() + "SERVER: Verbindung mit " + name + " beendet");
            }
        }
    }

    /**
     *
     */
    @Override
    public void updateRecord() {
        try {
            System.out.println(System.currentTimeMillis() + "Server - updateRecord");
//                       talkToAll(new Protokoll(MUSIC_SYSTEM, this.getMusicSystem()));
            talkToAll(new Protokoll(RECORD_DTO, musicSystem.getRecord().getDto()));
            talkToAll(new Protokoll(STATE, musicSystem.getMusicSystemState()));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Override
    public void updateTrack() {
        System.out.println(System.currentTimeMillis() + "Server - updateTrack");
        try {
//                       talkToAll(new Protokoll(MUSIC_SYSTEM, this.getMusicSystem()));
            talkToAll(new Protokoll(PLAY_LIST_COMPONENT_DTO, musicSystem.getCurrentTrack().getDto()));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateState() {
        System.out.println(System.currentTimeMillis() + "Server - updateState");
        try {
//                       talkToAll(new Protokoll(MUSIC_SYSTEM, this.getMusicSystem()));
            talkToAll(new Protokoll(STATE, (MusicSystemState) musicSystem.getMusicSystemState()));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    @Override
    public void updateTrackTime() {
        System.out.println(System.currentTimeMillis() + "Server - updateTrackTime");
        try {
            talkToAll(new Protokoll(TRACK_TIME, (Integer) musicSystem.getCurrentTimeTrack()));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateMusicPlayer() {
        System.out.println(System.currentTimeMillis() + "Server - updateMusicPlayer");
        //da sich die MusicPlayer ändert müssen die Observer, die an dem MusiicPlayer hängen neu registriert werden.
        //das stimmt doch gar nicht. this ist doch MusicServer -> dort werden sie aber an die activePlayer gehängt!!!!
        musicSystem.registerObserver((TrackObserver) this);
        musicSystem.registerObserver((StateObserver) this);
        musicSystem.registerObserver((TrackTimeObserver) this);
        musicSystem.registerObserver((VolumeObserver) this);
        musicSystem.registerObserver((RecordObserver) this);
        //Achtung - hier muss noch die passende MusicColleciton geladen werden.
        //Das ist keine tolle Lösung mir fällt nichts besseres ein.
        //Ich will vermeiden, das die MusicCollection an der MusicPlayer hängt.
        musicCollection = MusicCollection.getInstance(musicSystem.getActivePlayer().getClass().getSimpleName());

        try {
            talkToAll(new Protokoll(MUSIC_COLLECTION_DTO, this.getMusicCollection().getMusicCollectionDto()));

//            talkToAll(new Protokoll(MUSIC_SYSTEM, this.getMusicSystem()));
            talkToAll(new Protokoll(MUSIC_PLAYER_DTO, musicSystem.getActivePlayer().getDto()));
            talkToAll(new Protokoll(RECORD_DTO, musicSystem.getRecord().getDto()));
            talkToAll(new Protokoll(STATE, musicSystem.getMusicSystemState()));
//            talkToAll(new Protokoll(HAS_NEXT, musicSystem.hasNext()));
//            talkToAll(new Protokoll(HAS_PREVIOUS, musicSystem.hasPrevious()));
//            talkToAll(new Protokoll(HAS_PAUSE, musicSystem.hasPause()));
//            talkToAll(new Protokoll(HAS_TRACKS, musicSystem.hasTracks()));
//            talkToAll(new Protokoll(HAS_CURRENT_TIME, musicSystem.hasCurrentTime()));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the musicCollection
     */
    public MusicCollection getMusicCollection() {
        return (MusicCollection) musicCollection;
    }

    /**
     * @return the serverPool
     */
    public ServerPool getServerPool() {
        return serverPool;
    }

    public String getName() {
        return name;
    }

}
