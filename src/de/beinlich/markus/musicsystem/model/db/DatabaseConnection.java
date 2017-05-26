package de.beinlich.markus.musicsystem.model.db;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author IBB Teilnehmer
 */
public enum DatabaseConnection {
    INSTANCE;
    // Verbindungs-Parameter
    private String host;
    private String user;
    private String pass;
    private String db;
    
    // Verbindungs-Objekt
    private Connection connection;

    /**
     *
     */
    private DatabaseConnection() {
        if (!initConnection()) {
            throw new IllegalStateException("connection failed");
        }
    }
    
    private boolean initConnection() {
        // -OK- Verbindungs-Parameter in property-file auslagern
        File propertyFile = new File("db.properties");
        Properties dbProperties = new Properties();
        if (!propertyFile.exists()) {
            // erzeugen
            dbProperties.setProperty("db.host", "localhost");
            dbProperties.setProperty("db.user", "root");
            dbProperties.setProperty("db.pass", "");
            dbProperties.setProperty("db.db", "music");
            try {
                dbProperties.store(new FileWriter(propertyFile), "EIN KOMMENTAR");
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        
        try {
            // einlesen
            FileReader fr = new FileReader(propertyFile);
            dbProperties.load(fr);
            fr.close();
//            fr = new FileReader(propertyFile);
//            System.getProperties().load(fr);
//            fr.close();
//            System.getProperties().list(System.out);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        
        host = dbProperties.getProperty("db.host");
        user = dbProperties.getProperty("db.user");
        pass = dbProperties.getProperty("db.pass");
        db = dbProperties.getProperty("db.db");
        
        String url = "jdbc:mysql://" + host + "/" + db;
        
        try {
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLException ex) {
            System.out.println(ex);
            return false;
        }
        return true;
    }
    
    /**
     *
     * @return
     */
    public Connection getConnection() {
        return connection;
    }
    
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
        }
    }
}
