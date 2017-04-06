/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.mp3tag;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.beinlich.markus.musicsystem.model.Record;
import de.beinlich.markus.musicsystem.model.db.*;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Markus
 */
public class Mp3tag1 {

    Multimap<RecordDAO, TrackDAO> rc;
    Collection<TrackDAO> rc2;
    RecordCollectionConnector rcc;
    protected List<de.beinlich.markus.musicsystem.model.Record> records;

    // Verbindungs-Objekt
    private DatabaseConnection dbCon;

    // Statement-Objekt / Transport von Informationen ZUR datenbank
    private PreparedStatement stm;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new Mp3tag1().go();
    }

    public void go() {
        File propertyFile = new File("mp3tag.properties");
        Properties mp3TagProperties = new Properties();

        try {
            // einlesen
            FileReader fr = new FileReader(propertyFile);
            mp3TagProperties.load(fr);
            fr.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }

        rc = HashMultimap.create();

        try {
            System.out.println("mp3tag:" + mp3TagProperties.getProperty("mp3tag.file"));
            FileReader fr = new FileReader(mp3TagProperties.getProperty("mp3tag.file"));
            System.out.println("Encoding" + fr.getEncoding());
            LabeledCSVParser lcsvp = new LabeledCSVParser(
                    new ExcelCSVParser(fr)
            );
            lcsvp.changeDelimiter(';');

            while (lcsvp.getLine() != null) {
                System.out.println(":" + lcsvp.getValueByLabel("Titel") + lcsvp.getValueByLabel("Länge"));
                //Records ohne Title geben keinen Sinn und wenn die länge nicht nummerisch ist
                //gibt das auch keinen Sinn. (Normalerweise ist ein ; im Titel. Die sind nicht maskiert
                if ((!lcsvp.getValueByLabel("Titel").trim().equals("")) && (isInteger(lcsvp.getValueByLabel("Länge")))) {
                    rc.put(new RecordDAO(lcsvp.getValueByLabel("Album"),
                            lcsvp.getValueByLabel("Interpret"),
                            lcsvp.getValueByLabel("Pfad"),
                            "CD"),
                            new TrackDAO(0, 0, lcsvp.getValueByLabel("Titel"),
                                    Integer.parseInt(lcsvp.getValueByLabel("Länge")), 0,
                                    lcsvp.getValueByLabel("Pfad")+ lcsvp.getValueByLabel("Dateiname")));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Mp3tag1.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("RecordCollection:" + rc.size() + "-");// + rc.toString());
        System.out.println("keyset: " + rc.keySet().size() + "-");// + rc.keySet());

        dbCon = new DatabaseConnection();
        //einlesen der Records aus der Datenbank.
        rcc = new RecordCollectionConnector(dbCon);
        this.records = rcc.readRecords("CD");

        int rid = 0;
        int tn = 0;
        int uid = 0;
        int i = 0;

        for (RecordDAO record : rc.keySet()) {
            System.out.println("i:" + i++);
            System.out.println("Record: " + record);
            //Nur einfügen, wenn der Titel noch nicht als CD existiert
            if (!records.contains(new Record(record.getTitle()))) {
                rid = insertRecord(record);
                tn = 1;
                for (TrackDAO track : rc.get(record)) {
                    track.setRid(rid);
                    track.setTrackNumber(tn++);
                    System.out.println("Track: " + track);
                    uid = insertTrack(track);
                }
            }
        }
    }

    public int insertRecord(RecordDAO record) {
        int rid = 0;
        Connection con = null;
        ResultSet rs = null;
        FileInputStream gif = null;

        //Nach Cover-Bild suchen
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(record.getPath()), "*.jpg")) {
            for (Path path : stream) {
                if (Files.size(path) < 64_000) {
                    gif = new FileInputStream(path.toFile());
                } else {
                    System.out.println("Datei zu groß: " + path.toString());
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Mp3tag1.class.getName()).log(Level.SEVERE, "Pfad:" + record.getPath(), ex);
        }

        try {

            con = dbCon.getConnection();
            if (con == null) {
                return 0;
            }
            stm = con.prepareStatement("INSERT INTO record ("
                    + "ARTIST, "
                    + "MEDIUM, "
                    + "COVER, "
                    + "TITLE) VALUES(?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, record.getArtist());
            stm.setString(2, record.getMedium());
            stm.setBlob(3, gif);
            stm.setString(4, record.getTitle());

            int rows = stm.executeUpdate();

//            con.commit();
            //Vergebenen Schlüssel holen
            rs = stm.getGeneratedKeys();
            if (rs.next()) {
                rid = rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Mp3tag1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Mp3tag1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return rid;
    }

    public int insertTrack(TrackDAO track) {
        int uid = 0;
        Connection con = null;
        ResultSet rs = null;

        try {

            con = dbCon.getConnection();
            if (con == null) {
                return 0;
            }
            stm = con.prepareStatement("INSERT INTO track ("
                    + "PLAYINGTIME, "
                    + "RID, "
                    + "TRACKNUMBER, "
                    + "FILENAME, "
                    + "TITLE) VALUES(?,?,?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stm.setInt(1, track.getPlayingTime());
            stm.setInt(2, track.getRid());
            stm.setInt(3, track.getTrackNumber());
            stm.setString(4, track.getFilename());
            stm.setString(5, track.getTitle());

            int rows = stm.executeUpdate();

//            con.commit();
            //Vergebenen Schlüssel holen
            rs = stm.getGeneratedKeys();
            if (rs.next()) {
                uid = rs.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Mp3tag1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Logger.getLogger(Mp3tag1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return uid;
    }

    private static boolean isInteger(final String strInput) {
        boolean ret = true;
        try {
            Integer.parseInt(strInput);
        } catch (final NumberFormatException e) {
            ret = false;
        }
        return ret;
    }
}
