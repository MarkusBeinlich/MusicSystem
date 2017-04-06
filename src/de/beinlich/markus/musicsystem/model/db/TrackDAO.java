package de.beinlich.markus.musicsystem.model.db;

// Datensatz-Klasse
// DTO - data transfer object
// entity class

/**
 *
 * @author IBB Teilnehmer
 */
public class TrackDAO {

    private int rid;
    private int uid;
    private int trackNumber;
    private String title;
    private int playingTime;
    private int useCounter;
    private String filename;

    // Konstruktor ohne uid - wird aus der Anwedung/vom Benutzer verwendet

    /**
     *
     * @param rid
     * @param trackNumber
     * @param title
     * @param playingTime
     * @param useCounter
     */
    public TrackDAO(int rid, int trackNumber, String title, int playingTime, int useCounter, String filename) {
        this(0, rid, trackNumber, title, playingTime, useCounter, filename);
    }

    // Konstruktor mit uid: wird aus der Datenbank verwendet

    /**
     *
     * @param uid
     * @param rid
     * @param trackNumber
     * @param title
     * @param playingTime
     * @param useCounter
     */
    public TrackDAO(int uid, int rid, int trackNumber, String title, int playingTime, int useCounter, String filename) {
        this.uid = uid;
        this.rid = rid;
        this.trackNumber = trackNumber;
        this.title = title;
        this.playingTime = playingTime;
        this.useCounter = useCounter;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return getTrackNumber() + " " + getTitle() + " " + getPlayingTime();
    }

    /**
     * @return the rid
     */
    public int getRid() {
        return rid;
    }

    /**
     * @param rid the rid to set
     */
    public void setRid(int rid) {
        this.rid = rid;
    }

    /**
     * @return the uid
     */
    public int getUid() {
        return uid;
    }

    /**
     * @param uid the uid to set
     */
    public void setUid(int uid) {
        this.uid = uid;
    }

    /**
     * @return the trackNumber
     */
    public int getTrackNumber() {
        return trackNumber;
    }

    /**
     * @param trackNumber the trackNumber to set
     */
    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the playingTime
     */
    public int getPlayingTime() {
        return playingTime;
    }

    /**
     * @param playingTime the playingTime to set
     */
    public void setPlayingTime(int playingTime) {
        this.playingTime = playingTime;
    }

    /**
     * @return the useCounter
     */
    public int getUseCounter() {
        return useCounter;
    }

    /**
     * @param useCounter the useCounter to set
     */
    public void setUseCounter(int useCounter) {
        this.useCounter = useCounter;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

}
