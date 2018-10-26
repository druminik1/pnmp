package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import service.ServiceProvider;
import service.playback.AudioPlayer;

import java.io.File;
import java.io.InputStream;

/**
 * Main class of the Personalized Music Player(pnmp)
 * Entrypoint of the application
 *
 * @author Dominik Scheffknecht 1426857
 */
public class Main extends Application{

    /**
     * launches the application
     * @param args
     */
    public static void main(String args[]) {
        Application.launch(args);
    }


    /**
     * loads the application-windows
     * checks if librarypath and dbpath have been set yet, if not respective directory-choosers are opened first
     * @param primaryStage the application's primary stage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        setUserAgentStylesheet(STYLESHEET_MODENA);
        /* check if library and dbpath have been set */
        if (ServiceProvider.getPreferenceManager().getSet() == false) {

            /* chooser for musiclibrary */
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose A MusicLibraryRootDirectory");

            final File selectedDirectory = chooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                ServiceProvider.getPreferenceManager().setLibraryRoot(selectedDirectory.getAbsolutePath());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No MusicLibrary selected");
                alert.setHeaderText("Can't start application!");
                alert.setContentText("Select your music library root directory!");
                alert.showAndWait();
                System.exit(1);
            }

            /* chooser for database-directory */
            chooser = new DirectoryChooser();
            chooser.setTitle("Choose a directory for the Database");

            final File selectedDirectory1 = chooser.showDialog(primaryStage);
            if (selectedDirectory1 != null) {
                ServiceProvider.getPreferenceManager().setDbPath(selectedDirectory1.getAbsolutePath());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No DatabasePath selected");
                alert.setHeaderText("Can't start application!");
                alert.setContentText("Select your Database directory!");
                alert.showAndWait();
                System.exit(1);
            }
            /* save in the preference that directories have been set, so this part can be left out next time */
            ServiceProvider.getPreferenceManager().setSet(true);
        }

        /* instantiate an audioplayer */
        AudioPlayer player = ServiceProvider.getAudioPlayer();

        /* load the container-fxml */
        FXMLLoader loader = new FXMLLoader();
        InputStream fxmlStream = getClass().getResourceAsStream("/fxml/mainContainer.fxml");

        Parent root = loader.load(fxmlStream);
        Scene scene = new Scene(root);

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Personalized-Noise Player");
        primaryStage.show();
        primaryStage.setOnHidden(e -> player.stop()); //stop player when closing the application

    }


}
