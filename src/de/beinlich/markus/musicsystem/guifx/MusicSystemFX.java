package de.beinlich.markus.musicsystem.guifx;

import de.beinlich.markus.musicsystem.model.net.MusicClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Markus
 */
public class MusicSystemFX extends Application {

    static private MusicClient musicClient;
    static private RecordsModel recordsModel;
    static private PlayerModel playerModel;
    static private ServerModel serverModel;
    static private TrackListModel trackListModel;

    @Override
    public void start(Stage stage) throws Exception {
        musicClient = new MusicClient("FX-Client");
        recordsModel = new RecordsModel(musicClient);
        serverModel = new ServerModel(musicClient);
        playerModel = new PlayerModel(musicClient);
        trackListModel = new TrackListModel(musicClient);

        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle(musicClient.getMusicSystemName() + " - " + musicClient.getLocation() + " -  FX-Client" );
        stage.setScene(scene);
        stage.show();
    }

    public static RecordsModel getRecordsModel() {
        return recordsModel;
    }

    public static ServerModel getServerModel() {
        return serverModel;
    }

    public static PlayerModel getPlayerModel() {
        return playerModel;
    }

    public static TrackListModel getTrackListModel() {
        return trackListModel;
    }

    public static MusicClient getMusicClient() {
        return musicClient;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
