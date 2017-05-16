package de.beinlich.markus.musicsystem.model.db;

// Datensatz-Klasse
import java.sql.Blob;
import java.util.Objects;

// DTO - data transfer object
// entity class
/**
 *
 * @author IBB Teilnehmer
 */
public class RecordDAO {

    private int rid;
    private String title;
    private String artist;
    private String path;
    private String medium;
    private Blob cover;

    // Konstruktor ohne uid - wird aus der Anwedung/vom Benutzer verwendet
    /**
     *
     * @param title
     * @param artist
     * @param medium
     */
    public RecordDAO(String title, String artist, String path, String medium) {
        this(0, title, artist, path, medium, null);
    }

    public RecordDAO(String title, String artist, String path, String medium, Blob cover) {
        this(0, title, artist, path, medium, cover);
    }

    // Konstruktor mit uid: wird aus der Datenbank verwendet
    /**
     *
     * @param rid
     * @param title
     * @param artist
     * @param medium
     */
    public RecordDAO(int rid, String title, String artist, String path, String medium, Blob cover) {
        this.rid = rid;
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.medium = medium;
        this.cover = cover;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RecordDAO other = (RecordDAO) obj;
        if (this.rid != other.rid) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.rid;
        hash = 53 * hash + Objects.hashCode(this.title);
        return hash;
    }
    


    @Override
    public String toString() {
        return getMedium() + " " + getTitle() + " " + getArtist();
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
     * @return the artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @param artist the artist to set
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * @return the medium
     */
    public String getMedium() {
        return medium;
    }

    /**
     * @param medium the medium to set
     */
    public void setMedium(String medium) {
        this.medium = medium;
    }

    /**
     * @return the cover
     */
    public Blob getCover() {
        return cover;
    }

    /**
     * @param cover the cover to set
     */
    public void setCover(Blob cover) {
        this.cover = cover;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

}
