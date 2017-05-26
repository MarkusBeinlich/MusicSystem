package de.beinlich.markus.musicsystem.model.db;

import de.beinlich.markus.musicsystem.model.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author IBB Teilnehmer
 */
public class RecordCollectionConnector {

    // Verbindungs-Objekt
    private Connection connection;

    // Statement-Objekt / Transport von Informationen ZUR datenbank
    private Statement statement;

    public RecordCollectionConnector() {
        this(DatabaseConnection.INSTANCE);
    }

    public RecordCollectionConnector(DatabaseConnection dbc) {
        this(dbc.getConnection());
    }

    public RecordCollectionConnector(Connection connection) {
        try {
            this.connection = connection;
            statement = connection.createStatement();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    /**
     *
     * @param uid
     * @return
     */
    public boolean incrementUseCounter(int uid) {
        System.out.println(System.currentTimeMillis() + "IncrementUseCounter:" + uid);
        String sql = "UPDATE `track` SET `USECOUNTER`= `USECOUNTER`+ 1 WHERE `UID` =" + uid;
        try {
            return (1 == statement.executeUpdate(sql)) ? true : false;
        } catch (SQLException ex) {
            System.out.println(ex);
            return false;
        }

    }

    /**
     *
     * @return
     */
    public List<Record> readRadios() {
        List<Record> radios = new ArrayList<>();
        Record rec;
        PlayListComponent radio;
        int uid = 0;
        String sql = "SELECT * FROM `radio` ";
        System.out.println(System.currentTimeMillis() + "-----------------readRadios");
        rec = new Record(-1, "Radios", "", null);
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                radio = new RadioStation(
                        uid++,
                        resultSet.getString("title"),
                        resultSet.getString("frequency")
                );
                rec.addTrack((PlayListComponentInterface) radio);
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            return null;
        }
        radios.add(rec);

        return radios;
    }

    /**
     *
     * @param medium
     * @return
     */
    public List<Record> readRecords(String medium) {
        List<Record> records = new ArrayList<>();
        Record rec;
        Track track;

        String sql = "SELECT `record`.*, `track`.* FROM `track` LEFT JOIN `record` ON `track`.`RID` = `record`.`RID` WHERE `MEDIUM` =\"" + medium + "\" ORDER BY `track`.`RID` ASC, `TRACKNUMBER` ASC";
        System.out.println(System.currentTimeMillis() + "readRecords-----------------------------------------");
        try {
            int rid = -1;
            ResultSet resultSet = statement.executeQuery(sql);
            rec = null;
            while (resultSet.next()) {
                //Bei Gruppenwechsel neuen Record anlegen
                if (rid != resultSet.getInt("rid")) {
                    rid = resultSet.getInt("rid");
                    rec = new Record(
                            rid,
                            resultSet.getString("title"),
                            resultSet.getString("artist"),
                            resultSet.getBytes("cover")
                    );
                    records.add(rec);
                }
                track = new Track(
                        resultSet.getString("track.title"),
                        resultSet.getInt("playingTime"),
                        resultSet.getInt("uid"),
                        resultSet.getString("filename")
                );
                if (rec != null) {
                    rec.addTrack((PlayListComponentInterface) track);
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            return null;
        }

        return records;
    }

    public List<Record> readMp3s() {
        List<Record> records = new ArrayList<>();
        Record rec;
        Track track;

        String sql = "SELECT `record`.*, `track`.* FROM `track` LEFT JOIN `record` ON `track`.`RID` = `record`.`RID` WHERE `FILENAME` IS NOT NULL ORDER BY `track`.`RID` ASC, `TRACKNUMBER` ASC";
        System.out.println(System.currentTimeMillis() + "readMp3s---------------------------------------------");
        try {
            int rid = -1;
            ResultSet resultSet = statement.executeQuery(sql);
            rec = null;
            while (resultSet.next()) {
                //Bei Gruppenwechsel neuen Record anlegen
                if (rid != resultSet.getInt("rid")) {
                    rid = resultSet.getInt("rid");
                    rec = new Record(
                            rid,
                            resultSet.getString("title"),
                            resultSet.getString("artist"),
                            resultSet.getBytes("cover")
                    );
                    records.add(rec);
                }
                track = new Track(
                        resultSet.getString("track.title"),
                        resultSet.getInt("playingTime"),
                        resultSet.getInt("uid"),
                        resultSet.getString("filename")
                );
                if (rec != null) {
                    rec.addTrack((PlayListComponentInterface) track);
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex);
            return null;
        }

        return records;
    }

    private boolean modify(String sql) {
        try {
            return statement.executeUpdate(sql) == 1;
        } catch (SQLException ex) {
            System.out.println(ex);
            return false;
        }
    }

}
