/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.beinlich.markus.musicsystem.guifx;

import de.beinlich.markus.musicsystem.model.*;
import de.beinlich.markus.musicsystem.model.net.*;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.STYLESHEET_CASPIAN;
import static javafx.application.Application.STYLESHEET_MODENA;
import static javafx.application.Application.setUserAgentStylesheet;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.Stage;

/**
 *
 * @author Markus
 */
public class FXMLDocumentController implements Initializable, VolumeObserver, TrackObserver, RecordObserver, MusicPlayerObserver, MusicCollectionObserver, ServerPoolObserver {

    private Label label;
    @FXML
    private Button buttonPlay;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonPrevious;
    @FXML
    private Button buttonPause;
    @FXML
    private Button buttonStop;
    @FXML
    private Button buttonCSS;
    @FXML
    private Slider sliderVolume;
    @FXML
    private Slider sliderProgress;
    @FXML
    private ImageView cover;
    @FXML
    private ComboBox<String> comboBoxServer;
    @FXML
    private ComboBox<MusicPlayerInterface> comboBoxPlayer;
    @FXML
    private ComboBox<RecordInterface> comboBoxRecords;
    @FXML
    private Label labelCurrentTrack;
    @FXML
    private Label labelElapsedTime;
    @FXML
    private Label labelRemainingTime;
    @FXML
    private ScrollPane scrollPaneTrackList;
    @FXML
    private ListView<PlayListComponentInterface> listViewTrackList;

    private MusicSystemInterface musicSystem;
    private MusicCollectionInterface musicCollection;
    private MusicSystemControllerInterface musicSystemController;
    private MusicClient musicClient;
    private RecordsModel recordsModel;
    private ServerModel serverModel;
    private PlayerModel playerModel;
    private TrackListModel trackListModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        musicClient = MusicSystemFX.getMusicClient();

        System.out.println(System.currentTimeMillis() + "**************MusicClient ist aktiv");
        musicSystem = musicClient;
        musicSystemController = musicClient;
        musicCollection = musicClient;

        recordsModel = MusicSystemFX.getRecordsModel();
        serverModel = MusicSystemFX.getServerModel();
        playerModel = MusicSystemFX.getPlayerModel();
        trackListModel = MusicSystemFX.getTrackListModel();

        labelElapsedTime.textProperty().bind(musicClient.getTrackTime().asString());
        labelRemainingTime.textProperty()
                .bind((musicClient.getTrackTime().subtract(musicSystem.getCurrentTrack().getPlayingTime()).asString()));


        buttonPlay.setOnAction(event -> musicSystemController.play());
        buttonStop.setOnAction(event -> musicSystemController.stop());
        buttonNext.setOnAction(event -> musicSystemController.next());
        buttonPrevious.setOnAction(event -> musicSystemController.previous());
        buttonPause.setOnAction(event -> musicSystemController.pause());
        buttonCSS.setOnAction(event -> {
            if (Application.getUserAgentStylesheet().equals(STYLESHEET_CASPIAN)) {
                setUserAgentStylesheet(STYLESHEET_MODENA);
            } else {
                setUserAgentStylesheet(STYLESHEET_CASPIAN);
            }
        });

        sliderVolume.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            musicSystemController.setVolume(sliderVolume.getValue());
        });
        
        sliderProgress.valueProperty().bindBidirectional(musicClient.getTrackTime());
        sliderProgress.setOnScrollFinished((event) -> {
                musicSystemController.seek((int) sliderProgress.getValue());
        });

        comboBoxPlayer.valueProperty().addListener((ObservableValue<? extends MusicPlayerInterface> observable, MusicPlayerInterface oldValue, MusicPlayerInterface newValue) -> {
            musicSystemController.setActivePlayer(newValue.getTitle());
        });
        comboBoxRecords.valueProperty().addListener((observable, oldValue, newValue) -> {
            musicSystemController.setRecord(newValue);
        });
        comboBoxServer.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(musicClient.getCurrentServerAddr().getName())) {
                if (false == musicClient.switchToServer(newValue)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Server " + newValue + " ist im Moment nicht erreichbar. Eventuell ist er nicht gestartet.");
//                    comboBoxServer.getSelectionModel().select(oldValue);
                } else {
                    System.out.println(System.currentTimeMillis() + "**************MusicClient ist aktiv");
                    doClientInit();

                }
            }
        });
        listViewTrackList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        musicSystemController.setCurrentTrack(newValue);
                    }
                });

        musicSystem.registerObserver((VolumeObserver) this);
//        musicSystem.registerObserver((TrackTimeObserver) this);
        musicSystem.registerObserver((TrackObserver) this);
        musicSystem.registerObserver((RecordObserver) this);
        musicSystem.registerObserver((MusicPlayerObserver) this);
        musicSystem.registerObserver((ServerPoolObserver) this);
        musicCollection.registerObserver((MusicCollectionObserver) this);

        System.out.println(System.currentTimeMillis() + "musicSystem ist übergeben:" + musicSystem);

        doClientInit();

    }

    private void doClientInit() {

        comboBoxPlayer.setItems(playerModel.getPlayer());
        listViewTrackList.setItems(trackListModel.getTracks());
        updateRecord();
        updateServerPool();
        updateMusicCollection();
        updateMusicPlayer();
        updateTrack();
        updateTrackTime();
        updateVolume();
    }

    @Override
    public void updateVolume() {
        Platform.runLater(() -> sliderVolume.setValue(musicSystem.getVolume()));
    }

    @Override
    public void updateTrack() {
        System.out.println("updatePlayListComponent:" + musicSystem.getCurrentTrack());
        if (musicSystem.getCurrentTrack() != null) {
            //richtigen Track selektieren - Objektgleichheit ist leider nicht gegeben
            for (int i = 0; i < listViewTrackList.getItems().size(); i++) {
                if (listViewTrackList.getItems().get(i).getUid() == musicSystem.getCurrentTrack().getUid()) {
                    listViewTrackList.getSelectionModel().select(i);
                    break;
                }
            }
            Platform.runLater(() -> {
                labelCurrentTrack.setText(musicSystem.getCurrentTrack().getTitle() + " : " + musicSystem.getCurrentTrack().getPlayingTime());
                sliderProgress.setValue(0);
                sliderProgress.setMax(musicSystem.getCurrentTrack().getPlayingTime());
            });
        }
        updateTrackTime();
    }

    public void updateTrackTime() {
        Platform.runLater(() -> {
//            labelElapsedTime.setText("- " + musicSystem.getCurrentTimeTrack());
//            labelRemainingTime.setText(" " + (musicSystem.getCurrentTrack().getPlayingTime() - musicSystem.getCurrentTimeTrack()));
//            sliderProgress.setValue((int) musicSystem.getCurrentTimeTrack());
        });
    }

    @Override
    public void updateRecord() {
        Platform.runLater(() -> {
            //richtigen Record selektieren - Objektgleichheit ist leider nicht gegeben
            for (int i = 0; i < comboBoxRecords.getItems().size(); i++) {
                if (comboBoxRecords.getItems().get(i).getTitle().equals(musicSystem.getRecord().getTitle())) {
                    comboBoxRecords.getSelectionModel().select(i);
                    break;
                }
            }
            listViewTrackList.setItems(FXCollections.observableArrayList(musicSystem.getRecord().getTracks()));
            listViewTrackList.refresh();
            cover.setImage(showCover());
        });
        System.out.println(System.currentTimeMillis() + "setRecord: " + musicSystem.getRecord() + " - " + trackListModel.getTracks().toString());
    }

    private Image showCover() {
        Image img;
        img = null;
        if (hasCover()) {
            img = new Image(new ByteArrayInputStream(musicSystem.getRecord().getCover()), 150, 150, true, true);
        }
        return img;
    }

    private boolean hasCover() {
        return (musicSystem.getRecord().getCover() != null);
    }

    @Override
    public void updateMusicPlayer() {
        System.out.println(System.currentTimeMillis() + "UpdateMusicPlayer: " + musicSystem.getActivePlayer());
        Platform.runLater(() -> {
            Stage stage = (Stage) buttonPlay.getScene().getWindow();
            stage.setTitle(musicClient.getMusicSystemName() + " - " + musicClient.getLocation() + " -  FX-Client");
            //Werte der aktiven MusicPlayer anzeigen
            buttonPlay.setDisable(!musicSystem.hasPlay());
            buttonStop.setDisable(!musicSystem.hasStop());
            buttonNext.setDisable(!musicSystem.hasNext());
            buttonPrevious.setDisable(!musicSystem.hasPrevious());
            buttonPause.setDisable(!musicSystem.hasPause());
            sliderProgress.setDisable(!musicSystem.hasCurrentTime());
//        jScrollPane1.setEnabled(musicSystem.hasTracks());
//        listCurrentRecord.setEnabled(musicSystem.hasTracks());
            //richtigen Player selektieren - Objektgleichheit ist leider nicht gegeben
            for (int i = 0; i < comboBoxPlayer.getItems().size(); i++) {
                if (comboBoxPlayer.getItems().get(i).getTitle().equals(musicSystem.getActivePlayer().getTitle())) {
                    comboBoxPlayer.getSelectionModel().select(i);
                    break;
                }
            }
        });
        System.out.println(System.currentTimeMillis() + "UpdateMusicPlayerEnd: " + comboBoxPlayer.getValue());
    }

    @Override
    public void updateMusicCollection() {
        Platform.runLater(() -> comboBoxRecords.setItems(recordsModel.getRecords()));
    }

    @Override
    public void updateServerPool() {
        Platform.runLater(() -> comboBoxServer.setItems(serverModel.getServer()));
    }

}
