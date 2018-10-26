package gui;

import exceptions.PlaybackException;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import persistence.PersistenceManager;
import service.ServiceProvider;
import service.playback.AudioPlayer;

/**
 * FXML controller for the player window
 * shows a rotating record while playing back audio
 *
 * @author Dominik Scheffknecht 1426857
 */
public class PlayerController  {

    private AudioPlayer player;
    private String track;
    private RotateTransition rt;
    private boolean running = false;
    private NoiseEditorController noiseEditorController;


    @FXML
    Button btn_stop_play;

    @FXML
    ImageView vinyl;

    @FXML
    Slider volume_slider;

    /**
     * creates the {@link RotateTransition} for the record image
     * and adds a event listener to the volume slider
     */
    @FXML
    public void initialize(){
        player = ServiceProvider.getAudioPlayer();
        rt = new RotateTransition(Duration.millis(3000),vinyl);
        rt.setByAngle(360);
        rt.setCycleCount(1000);
        rt.setInterpolator(Interpolator.LINEAR);

        volume_slider.setMin(0);
        volume_slider.setMax(1);
        volume_slider.setValue(1);
        volume_slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                player.setVolume(newValue.floatValue());
            }
        });
    }

    /**
     * called from {@link MusicLibraryController}
     * sets up the {@link AudioPlayer} with the track given as method parameter and starts playing the song
     * also starts the {@link RotateTransition}
     *
     * @param trackPath path of the song which should be played
     */
    public void playTrack(String trackPath){
        try {
            PersistenceManager persistenceManager = ServiceProvider.getPersistenceManager();
            this.track = trackPath;
            if (running)
                player.stop();
            player.setUp(trackPath);
            player.play();
            rt.play();
            running = true;
            noiseEditorController.selectNoiseProfile(trackPath);
        } catch (PlaybackException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("There was an error setting up the player:");
            alert.setTitle("Playback Exception");
            alert.setContentText(e.getCause().getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    /**
     * called on start/stop-button-action
     * stops the playback and the rotation if running
     * restarts the playback and the rotation if not running
     */
    @FXML
    public void startStopPlayback(){
        try {
            if (running) {
                player.stop();
                btn_stop_play.setText("Play");
                running = false;
                rt.stop();
            } else {
                player.setUp(track);
                player.play();
                btn_stop_play.setText("Stop");
                running = true;
                rt.play();
                noiseEditorController.selectNoiseProfile(track);
            }
        } catch (PlaybackException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("There was an error resetting the player:");
            alert.setTitle("Playback Exception");
            alert.setContentText(e.getCause().getMessage());
            alert.showAndWait();
        }

    }

    public void setNoiseEditorController(NoiseEditorController noiseEditorController) {
        this.noiseEditorController = noiseEditorController;
    }
}
