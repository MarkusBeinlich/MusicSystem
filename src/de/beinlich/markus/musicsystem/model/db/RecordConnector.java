package de.beinlich.markus.musicsystem.model.db;

// DAO - data access object
// TODO - externes Connection handling
// TODO - prepared Statements
import java.sql.*;
import java.util.*;

/**
 *
 * @author IBB Teilnehmer
 */
public class RecordConnector implements AutoCloseable {

    // Verbindungs-Parameter
    private String host;
    private String user;
    private String pass;
    private String db;

    // Verbindungs-Objekt
    private Connection connection;

    // Statement-Objekt / Transport von Informationen ZUR datenbank
    private Statement statement;

    /**
     *
     */
    public RecordConnector() {
        if (!initConnection()) {
            throw new IllegalStateException("connection failed");
        }
    }
    
    private boolean initConnection() {
        // TODO Verbindungs-Parameter in property-file auslagern
        host = "localhost";
        user = "root";
        pass = "";
        db = "music";
        
        String url = "jdbc:mysql://" + host + "/" + db;
        
        try {
            connection = DriverManager.getConnection(url, user, pass);
            //connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
        } catch (SQLException ex) {
            System.out.println();
            return false;
        }
        return true;
    }

    // Kommunikation zwiwschen Datenbank (Tabelle) und Anwendung
    /*
    
    - OK - Datenbank-Tabelle auslesen - alle Datensätze
    -    - (Datenbank-Tabelle auslesen - nur einen Datensatz nach uid)
    - OK - Datensatz hinzufügen
    - OK - Datensatz aktualisieren
    - OK - Datensatz löschen
    
     */
    /**
     *
     * @return
     */
    public List<RecordDAO> readRecords() {
        List<RecordDAO> records = new ArrayList<>();
        
        String sql = "SELECT * FROM record";
        
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                records.add(new RecordDAO(
                        resultSet.getInt("rid"),
                        resultSet.getString("title"),
                        resultSet.getString("artist"),
                        " ",
                        resultSet.getString("medium"),
                        resultSet.getBlob("cover")
                ));
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
    
    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
        }
    }
}
