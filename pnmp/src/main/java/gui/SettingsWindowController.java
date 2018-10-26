package gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import service.ServiceProvider;
import service.util.PreferenceManager;

import java.io.File;
import java.text.DecimalFormat;

/**
 * FXML controller for the settings window
 * here player settings can be changed
 * the settings available are the paths to the musiclibrary and the database
 * and the degree of wear (parameter which specifies how often noises are added to the profile)
 * the degree of wear can be set to three predefined profiles or a custom profile
 * with the custom setting, the user can set the parameters pNewScratch(val between 0-1), pNewConstantNoise(val between 0-1), incAmpAfterTimesPlayed(val between 0-100) by himself
 * all settings change are saved in the preference via the preferenceManager
 *
 * @author Dominik Scheffknecht 1426857
 */
public class SettingsWindowController {


    @FXML
    Slider s_pNewScratch;
    @FXML
    Slider s_pNewConstantNoise;
    @FXML
    Slider s_incAmp;
    @FXML
    VBox vb_advancedSettings;
    @FXML
    ChoiceBox cb_degreeOfWear;
    @FXML
    Label l_pNewScratch;
    @FXML
    Label l_pNewConstantNoise;
    @FXML
    Label l_incAmp;
    @FXML
    Label l_libraryDir;
    @FXML
    Label l_dbDir;

    private PreferenceManager preferenceManager;


    /**
     * adds listeners to the fxml-inputs and fills the degreeofwear choicebox
     * reads the settings from the preference and selects them in the settings window
     */
    @FXML
    public void initialize() {

        /* load the PreferenceManager */
        preferenceManager = ServiceProvider.getPreferenceManager();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        /* new scratch slider */
        s_pNewScratch.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                preferenceManager.setPNewScratch(newValue.doubleValue());
                /* show the value next to the slider */
                l_pNewScratch.setText(decimalFormat.format(newValue));

            }
        });

        /* new constant noise slider */
        s_pNewConstantNoise.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                preferenceManager.setPNewConstNoise(newValue.doubleValue());
                /* show the value next to the slider */
                l_pNewConstantNoise.setText(decimalFormat.format(newValue));
            }
        });

        /* inc amp slider */
        s_incAmp.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                preferenceManager.setIncAmpAfterTimesPlayed(newValue.intValue());
                /* show the value next to the slider */
                l_incAmp.setText(Integer.toString(newValue.intValue()));
            }
        });

        /* set the values specified in the preference */
        s_pNewScratch.setValue(preferenceManager.getPNewScratch());
        s_pNewConstantNoise.setValue(preferenceManager.getPNewConstNoise());
        s_incAmp.setValue(preferenceManager.getIncAmpAfterTimesPlayed());

        /* add event listener for the degreeofwear choicebox */
        cb_degreeOfWear.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {

                /* show custom sliders if custom was selected */
                if (newValue.equals("custom")) {
                    vb_advancedSettings.setVisible(true);
                } else {
                    vb_advancedSettings.setVisible(false);
                }

                /* set settings  if a predefined profile has been selected */
                if (newValue.equals("light")) {
                    s_pNewScratch.setValue(0.05);
                    s_pNewConstantNoise.setValue(0.01);
                    s_incAmp.setValue(100);
                } else if (newValue.equals("medium")) {
                    s_pNewScratch.setValue(0.2);
                    s_pNewConstantNoise.setValue(0.1);
                    s_incAmp.setValue(50);
                } else if (newValue.equals("heavy")) {
                    s_pNewScratch.setValue(0.3);
                    s_pNewConstantNoise.setValue(0.4);
                    s_incAmp.setValue(20);
                }
                preferenceManager.setDegreeOfWear(newValue.toString());
            }
        });

        /* fill and select degrreeOfWear */
        cb_degreeOfWear.setItems(FXCollections.observableArrayList("strong","medium","light","custom"));
        cb_degreeOfWear.getSelectionModel().select(preferenceManager.getDegreeOfWear());

        /* show the selected paths */
        l_libraryDir.setText(preferenceManager.getLibraryRoot());
        l_dbDir.setText(preferenceManager.getDbPath());
    }

    /**
     * button action to chose a new libraryRoot-directory
     * shows a new directory chooser
     */
    @FXML
    public void chooseLibraryDirectory() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final File selectedDirectory = directoryChooser.showDialog((Stage) vb_advancedSettings.getScene().getWindow());
        if (selectedDirectory != null) {
            preferenceManager.setLibraryRoot(selectedDirectory.getAbsolutePath());
            l_libraryDir.setText(selectedDirectory.getAbsolutePath());
        }
    }

    /**
     * button action to chose a new database-directory
     * shows a new directory chooser
     */
    @FXML
    public void chooseDbDirectory() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final File selectedDirectory = directoryChooser.showDialog((Stage) vb_advancedSettings.getScene().getWindow());
        if (selectedDirectory != null) {
            preferenceManager.setLibraryRoot(selectedDirectory.getAbsolutePath());
            l_dbDir.setText(selectedDirectory.getAbsolutePath());
        }
    }
}
