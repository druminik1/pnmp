package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

/**
 * FXML controller for the container window
 * here all fxmls of the main application are loaded
 *
 * @author Dominik Scheffknecht 1426857
 */
public class MainContainerController {

    @FXML
    AnchorPane p_library;

    @FXML
    AnchorPane p_editor;

    @FXML
    AnchorPane p_player;

    private PlayerController playerController;
    private NoiseEditorController noiseEditorController;
    private MusicLibraryController musicLibraryController;

    /**
     * loads the three main windows of the application
     * passes controllers created to other controllers if needed
     * For example the {@link MusicLibraryController} needs the {@link NoiseEditorController} and the {@link PlayerController} to select and play tracks
     *
     * @throws IOException if fxml can not be loaded
     */
    @FXML
    public void initialize() throws IOException {

        FXMLLoader loader = new FXMLLoader();
        InputStream fxmlStream = getClass().getResourceAsStream("/fxml/noiseEditor.fxml");
        AnchorPane newPane = loader.load(fxmlStream);
        noiseEditorController = loader.<NoiseEditorController>getController();
        p_editor.getChildren().setAll(newPane);

        loader = new FXMLLoader();
        fxmlStream = getClass().getResourceAsStream("/fxml/player.fxml");
        newPane = loader.load(fxmlStream);
        playerController = loader.<PlayerController>getController();
        playerController.setNoiseEditorController(noiseEditorController);
        p_player.getChildren().setAll(newPane);

        loader = new FXMLLoader();
        fxmlStream = getClass().getResourceAsStream("/fxml/musicLibrary.fxml");
        newPane = loader.load(fxmlStream);
        musicLibraryController = loader.<MusicLibraryController>getController();
        musicLibraryController.setNoiseEditorController(noiseEditorController);
        musicLibraryController.setPlayerController(playerController);
        p_library.getChildren().setAll(newPane);
    }

    /**
     * shows the settings-window if the button was clicked
     * loads the settingsWindow.fxml and shows it
     * when returning the musicLibraryController is re-initialized, just in case the libraryRoot has been changed
     * @throws IOException
     */
    @FXML
    public void showSettingsWindow() throws IOException{
        FXMLLoader loader = new FXMLLoader();
        InputStream fxmlStream = (getClass().getResourceAsStream("/fxml/settingsWindow.fxml"));
        Parent root1 =  loader.load(fxmlStream);
        Stage stage = new Stage();
        stage.setTitle("Settings");
        stage.setScene(new Scene(root1));
        stage.showAndWait();
        musicLibraryController.initialize();
    }
}
