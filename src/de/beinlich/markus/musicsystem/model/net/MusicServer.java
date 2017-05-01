package de.beinlich.markus.musicsystem.model.net;

import de.beinlich.markus.musicsystem.lib.NetProperties;
import de.beinlich.markus.musicsystem.lib.MusicSystemControllerInterface;
import de.beinlich.markus.musicsystem.lib.StateObserver;
import de.beinlich.markus.musicsystem.lib.MusicPlayerObserver;
import de.beinlich.markus.musicsystem.lib.MusicCollectionObserver;
import de.beinlich.markus.musicsystem.lib.MusicSystemInterfaceObserver;
import de.beinlich.markus.musicsystem.lib.PlayListComponentInterface;
import de.beinlich.markus.musicsystem.lib.VolumeObserver;
import de.beinlich.markus.musicsystem.lib.TrackTimeObserver;
import de.beinlich.markus.musicsystem.lib.RecordObserver;
import de.beinlich.markus.musicsystem.lib.TrackObserver;
import de.beinlich.markus.musicsystem.lib.RecordInterface;
import de.beinlich.markus.musicsystem.lib.MusicCollectionInterface;
import de.beinlich.markus.musicsystem.lib.ClientInit;
import de.beinlich.markus.musicsystem.lib.MusicPlayerInterface;
import de.beinlich.markus.musicsystem.lib.ServerPool;
import de.beinlich.markus.musicsystem.lib.ServerAddr;
import de.beinlich.markus.musicsystem.lib.MusicSystemState;
import de.beinlich.markus.musicsystem.lib.MusicSystemInterface;
import de.beinlich.markus.musicsystem.lib.Protokoll;
import de.beinlich.markus.musicsystem.model.*;
import static de.beinlich.markus.musicsystem.lib.ProtokollType.*;
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
        serverPool = ServerPool.getInstance(name);

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

            Thread musicServerFinderThread = new Thread(new MusicServerFinder());
            musicServerFinderThread.setDaemon(true);
            musicServerFinderThread.start();

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

    public class MusicServerFinder implements Runnable {

        @Override
        public void run() {
            //Nach weiteren aktiven IP-Adresse im LAN suchen 
            tryAllAddressesOnLan();

        }

        private void tryAllAddressesOnLan() {
            InetAddress localhost;
            try {
                localhost = InetAddress.getLocalHost();
            } catch (UnknownHostException ex) {
                Logger.getLogger(MusicServer.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            byte[] ip = localhost.getAddress();

            for (int i = 1; i <= 254; i++) {
                try {
                    ip[3] = (byte) i;
                    InetAddress address = InetAddress.getByAddress(ip);
                    tryAddress(address);
                } catch (UnknownHostException e) {
                    Logger.getLogger(MusicServer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }

        private void tryAddress(InetAddress address) {
            try {
                if (address.isReachable(10)) {
                    System.out.println(address.toString().substring(1) + " is on the network");
                    tryAllPorts(address.getHostAddress());
                }
            } catch (IOException ex) {
                Logger.getLogger(MusicServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void tryAllPorts(String hostAddress) {
            for (int j = 1; j <= 3; j++) {
                tryToConnectServer(hostAddress, 50000 + j);
            }
        }

        private void tryToConnectServer(String hostAddress, int port) {
            Socket socket;
            try {
                socket = new Socket(hostAddress, port);
                System.out.println(System.currentTimeMillis() + "socket.connect");
                new Thread(new ClientHandler(socket, MusicServer.this, true)).start();
            } catch (ConnectException e) {
                System.out.println(System.currentTimeMillis() + "Error while connecting. " + e.getMessage());
            } catch (SocketTimeoutException e) {
                System.out.println(System.currentTimeMillis() + "Connection: " + e.getMessage() + ".");
            } catch (IOException e) {
                Logger.getLogger(MusicServer.class.getName()).log(Level.SEVERE, null, e);
            }
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
            } catch (IOException ex) {
                Logger.getLogger(MusicServer.class.getName()).log(Level.SEVERE, null, ex);
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
            talkToAll(new Protokoll(RECORD_DTO, musicSystem.getRecord().getDto()));
            talkToAll(new Protokoll(STATE, musicSystem.getMusicSystemState()));

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
