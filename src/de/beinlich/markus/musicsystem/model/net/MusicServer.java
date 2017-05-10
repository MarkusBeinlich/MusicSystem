package de.beinlich.markus.musicsystem.model.net;

import de.beinlich.markus.musicsystem.model.*;
import static de.beinlich.markus.musicsystem.model.net.ProtokollType.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.SwingWorker;

public class MusicServer extends SwingWorker<Void, Void> implements VolumeObserver, TrackTimeObserver, TrackObserver, StateObserver, RecordObserver, MusicPlayerObserver, MusicCollectionObserver {

    private final List<ObjectOutputStream> clients;
    private final List<ObjectOutputStream> servers;

    private final MusicSystemInterfaceObserver musicSystem; //Model
    private final MusicSystemControllerInterface musicSystemController;
    private MusicCollectionInterface musicCollection;
    private final ServerPool serverPool;
    private final ServerFinder serverFinder;
    private final String name;

    public MusicServer(MusicSystemControllerInterface musicSystemController, MusicSystemInterfaceObserver musicSystem) {
//        this.msa = msa;
        clients = new ArrayList<>();
        servers = new ArrayList<>();
        this.musicSystem = musicSystem;
        this.musicSystemController = musicSystemController;
        //Den Namen des MusicServer auf den gleichen wert wie beim MusicSystem setzen.
        //Der Name wird für den Dateinamen des ServerPool verwendet.
        name = musicSystem.getMusicSystemName();
        musicCollection = MusicCollectionCreator.getInstance(musicSystem.getActivePlayer().getClass().getSimpleName());
        musicSystem.setRecord((Record) musicCollection.getRecord());
        serverPool = ServerPool.getInstance();
        serverFinder = new ServerFinder(serverPool, musicSystem.getServerAddr());
        serverFinder.findServers();

        musicSystem.registerObserver((VolumeObserver) this);
        musicSystem.registerObserver((TrackTimeObserver) this);
        musicSystem.registerObserver((TrackObserver) this);
        musicSystem.registerObserver((StateObserver) this);
        musicSystem.registerObserver((RecordObserver) this);
        musicSystem.registerObserver((MusicPlayerObserver) this);
        musicCollection.registerObserver((MusicCollectionObserver) this);
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

//            serverPool.findServers();
//            Thread musicServerFinderThread = new Thread(new MusicServerFinder());
//            musicServerFinderThread.setDaemon(true);
//            musicServerFinderThread.start();
            Socket socket;
            while (true) {
                System.out.println(System.currentTimeMillis() + "Server lauscht!");

                // waiting for a client to connect
                socket = serverSocket.accept();  // blockiert!

                new Thread(new ClientHandler(socket, this, false)).start();

            }
        } catch (IOException | NumberFormatException ex) {
            System.out.println(ex);
        }
        return null;
    }

    @Override
    public void done() {
        System.out.println(System.currentTimeMillis() + "MusicServer done");
    }

    private synchronized void talkToAll(Protokoll nachricht) {
        clients.forEach((oos) -> {
            try {
                oos.writeObject(nachricht);
                oos.flush();
                System.out.println(System.currentTimeMillis() + "server: geschrieben: " + nachricht + "-" + System.currentTimeMillis());
            } catch (IOException ex) {
                System.out.println(ex);
            }
        });
    }

    private synchronized void talkToAllServer(Protokoll nachricht) {
        servers.forEach((oos) -> {
            try {
                oos.writeObject(nachricht);
                oos.flush();
                System.out.println(System.currentTimeMillis() + "server: geschrieben: " + nachricht + "-" + System.currentTimeMillis());
            } catch (IOException ex) {
                System.out.println(ex);

            }
        });
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

        private Socket socket;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private String name;
        private MusicServer musicServer;
        private MusicSystemInterface musicSystem;
        private MusicSystemControllerInterface musicSystemController;

        public ClientHandler(Socket socket, MusicServer musicServer, boolean isServer) {
            this.socket = socket;
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
            } catch (IOException ex) {
                Logger.getLogger(MusicServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            Object o;
            Protokoll protokoll;
            ServerAddr serverAddr;
            Map<String, ServerAddr> servers;
            RecordInterface record;
            String musicPlayerTitle;
            MusicPlayerInterface musicPlayer;
            PlayListComponentInterface playListComponent;

            try {
                // Liest als erste Zeile den Namen des Client bzw des Servers
//                 

                System.out.println(System.currentTimeMillis() + "Warten auf Nachrichten");
                while (true) {

                    // warten auf client-nachrichten
                    o = ois.readObject();  // blockiert!
                    System.out.println(System.currentTimeMillis() + "Server gelesen Object:" + o);
                    protokoll = (Protokoll) o;
                    switch (protokoll.getProtokollType()) {
                        case SERVER_ADDR_REQUEST:
//                        serverAddr = (ServerAddr) protokoll.getValue();
//                        if (serverAddr != null) {
//                            ServerPool.getInstance(musicSystem.getServerAddr()).addServer(serverAddr.getName(), serverAddr);
//                            // Clients auch noch über den aktuellen Serverpool informieren
//                            talkToAll(new Protokoll(SERVER_POOL, ServerPool.getInstance(musicSystem.getServerAddr())));
//                        }
                            oos.writeObject(new Protokoll(SERVER_ADDR, musicSystem.getServerAddr()));
                            oos.flush();
                            break;
                        case SERVER_ADDR:
//                        servers.add(oos);
                            serverAddr = (ServerAddr) protokoll.getValue();
                            System.out.println(System.currentTimeMillis() + "SERVER: habe eine Verbindung mit " + serverAddr.getName());
                            ServerPool.getInstance().addServer(serverAddr.getName(), serverAddr);
                            //Information über aktiven Server an alle aktiven Server weitergeben.
                            //Anderen Protokolltype verwenden, damit keine Endlosschleife entsteht.
//                        talkToAllServer(new Protokoll(SERVER_POOL, ServerPool.getInstance(musicSystem.getServerAddr())));
                            // Clients auch noch über den aktuellen Serverpool informieren
                            talkToAll(new Protokoll(SERVER_POOL, ServerPool.getInstance().getServers()));
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
                            } catch (IOException ex) {
                                System.out.println(ex);
                            }
                            break;
                        case SERVER_POOL:
                            servers = (Map<String, ServerAddr>) protokoll.getValue();
                            ServerPool.getInstance().addServers(servers);
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
                        case TRACK_TIME:
                            musicSystemController.seek((int) protokoll.getValue());
                            System.out.println(System.currentTimeMillis() + "TRACK_TIME: " + protokoll.getValue());
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
                        case CLIENT_DISCONNECT:
                            clients.remove(oos);
                            socket.close();
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

    @Override
    public void updateMusicCollection() {
        try {
            System.out.println(System.currentTimeMillis() + "Server - updateMusicCollection");
            musicCollection = MusicCollectionCreator.getInstance(musicSystem.getActivePlayer().getClass().getSimpleName());
            talkToAll(new Protokoll(MUSIC_COLLECTION_DTO, musicCollection.getMusicCollectionDto()));
        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateRecord() {
        try {
            System.out.println(System.currentTimeMillis() + "Server - updateRecord");
            talkToAll(new Protokoll(RECORD_DTO, musicSystem.getRecord().getDto()));
            talkToAll(new Protokoll(STATE, musicSystem.getMusicSystemState()));
        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

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
        musicCollection = MusicCollectionCreator.getInstance(musicSystem.getActivePlayer().getClass().getSimpleName());
        musicCollection.registerObserver((MusicCollectionObserver) this);
        try {
            talkToAll(new Protokoll(MUSIC_PLAYER_DTO, musicSystem.getActivePlayer().getDto()));
//            talkToAll(new Protokoll(RECORD_DTO, musicSystem.getRecord().getDto()));
//            talkToAll(new Protokoll(PLAY_LIST_COMPONENT_DTO, musicSystem.getRecord().getTracks().get(0).getDto()));
//            talkToAll(new Protokoll(STATE, musicSystem.getMusicSystemState()));

        } catch (InvalidObjectException ex) {
            Logger.getLogger(MusicServer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the musicCollection
     */
    public MusicCollectionInterface getMusicCollection() {
        return musicCollection;
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
