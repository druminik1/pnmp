package gui;

import exceptions.PersistenceException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import persistence.PersistenceManager;
import persistence.model.Scratch;
import service.ServiceProvider;
import service.util.DirectoryParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * FXML controller for the noiseEditor window
 * here noisprofiles can be edited
 * every change is persisted to the database
 *
 * @author Dominik Scheffknecht 1426857
 */
public class NoiseEditorController {

    private ObservableList<Scratch> scratchObservableList;
    private PersistenceManager persistenceManager;
    private String selectedProfile;

    @FXML
    Label lb_title;
    @FXML
    ChoiceBox<String> cb_whiteNoise;
    @FXML
    ChoiceBox<String> cb_scratch;
    @FXML
    Slider s_whiteNoiseAmp;
    @FXML
    TableView tv_scratch;
    @FXML
    TableColumn tc_file;
    @FXML
    TableColumn tc_frame;
    @FXML
    TextField tf_start;

    /**
     * initializes tableView, fills choiceboxes and sets event listeners
     */
    @FXML
    public void initialize(){

        persistenceManager = ServiceProvider.getPersistenceManager();


        cb_whiteNoise.getItems().add("none");
        cb_whiteNoise.getItems().addAll(DirectoryParser.getFileList("/permanentNoises"));

        /*set white noise choicebox string converter */

        /* handle selection event */
        cb_whiteNoise.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                persistenceManager.setConstantNoise(newValue);
            }
        });

        /* handle amplifier change event */
        s_whiteNoiseAmp.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                persistenceManager.setAmplifier(newValue.intValue());
            }
        });

        /* fill scratch choice box */
        cb_scratch.getItems().addAll(DirectoryParser.getFileList("/scratches"));

        /* add deletion context menu to tableview */
        ContextMenu menu = new ContextMenu();
        MenuItem mi = new MenuItem("Delete");
        menu.getItems().add(mi);
        mi.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Scratch selected = (Scratch) tv_scratch.getSelectionModel().getSelectedItem();
                persistenceManager.removeScratch(selected);
                tv_scratch.refresh();
            }
        });
        tv_scratch.setContextMenu(menu);

        // force the field to be numeric only
        tf_start.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    tf_start.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    /**
     * method to select a new noiseprofile, e.g. invoked by the {@link MusicLibraryController}
     * tableview of scratches is filled and if active the constant noise is selected
     *
     * @param noiseProfileId if of the profile(song path) which should be selected
     */
    public void selectNoiseProfile(String noiseProfileId){
        try {
        persistenceManager.loadProfileFromDatabase(noiseProfileId);
        lb_title.setText(noiseProfileId.substring(noiseProfileId.lastIndexOf("\\") + 1));

        /* select constant noise */
        if (persistenceManager.getProfile().getConstantNoise() != null) {
            cb_whiteNoise.getSelectionModel().select(persistenceManager.getProfile().getConstantNoise());
        } else {
            cb_whiteNoise.getSelectionModel().select("none");
        }

        /* set amplifier */
        s_whiteNoiseAmp.setValue(persistenceManager.getProfile().getAmplifier());

        /* load scratches */
        List<Scratch> scratches = persistenceManager.getProfile().getScratches();
        scratchObservableList = FXCollections.observableList(scratches);
        tc_file.setCellValueFactory(new PropertyValueFactory<Scratch, String>("fileName"));
        tc_frame.setCellValueFactory(new PropertyValueFactory<Scratch, Integer>("startFrame"));
        tv_scratch.setItems(scratchObservableList);
        } catch (PersistenceException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Persistence Exception");
            alert.setHeaderText("Something went wrong while selecting noise-profile:");
            alert.setContentText(e.getCause().getMessage());
            alert.showAndWait();
        }
        selectedProfile = noiseProfileId;
    }

    /**
     * function to create a new scratch, called if the add button is clicked
     */
    @FXML
    public void createScratch(){

        /* check if there is a active profile */
        if (selectedProfile == null) {
            alertNoProfileSelected();
            return;
        }

        /* create the scratch with information given in the ui */
        Scratch scratch = new Scratch();
        try {
            int startFrame = Integer.parseInt(tf_start.getText());
            scratch.setStartFrame(startFrame);
            if (startFrame > persistenceManager.getProfile().getLenght() || startFrame < 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Invalid Startframe");
                alert.setHeaderText("Can not create new scratch!");
                alert.setContentText("Startframe is out of bounds (Length of the file: " + persistenceManager.getProfile().getLenght() + ")");
                alert.showAndWait();
                return;
            }
        } catch (PersistenceException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Persistence Exception");
            alert.setHeaderText("Something went wrong while searching noise-profile-informations:");
            alert.setContentText(e.getCause().getMessage());
            alert.showAndWait();
            return;
        }
        scratch.setFileName(cb_scratch.getSelectionModel().getSelectedItem());

        /* persist new scratch */
        persistenceManager.addScratch(scratch);
        tv_scratch.refresh();
    }

    /**
     * clears the active noiseprofile
     */
    @FXML
    public void clearScratches(){
        if (selectedProfile == null) {
            alertNoProfileSelected();
            return;
        }
        persistenceManager.clearProfile();
        cb_whiteNoise.getSelectionModel().select("none");
        s_whiteNoiseAmp.setValue(0);
        tv_scratch.refresh();
    }

    /**
     * generates a new noiseProfile
     * fist the current profile is cleared and then simulateAttrition is called depending on which grade of destruction was selected:
     * ligth - 20 times
     * medium - 50 times
     * heavy - 100 times
     */
    @FXML
    public void generateNewNoiseProfile(){
        if (selectedProfile == null) {
            alertNoProfileSelected();
            return;
        }
        clearScratches();
        List<String> choices = new ArrayList<>();
        choices.add("light");
        choices.add("medium");
        choices.add("heavy");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("ligth", choices);
        dialog.setTitle("Noise-Profile-Creator");
        dialog.setHeaderText("Generating new noise profile.");
        dialog.setContentText("Chose grade of damage:");

        Optional<String> result = dialog.showAndWait();
        try {
            if (result.isPresent()) {
                if (("light").equals(result.get())) {
                    for (int i = 0; i < 20; i++)
                        persistenceManager.simulateAttrition(selectedProfile);
                } else if (("medium").equals(result.get())) {
                    for (int i = 0; i < 50; i++)
                        persistenceManager.simulateAttrition(selectedProfile);
                } else {
                    for (int i = 0; i < 100; i++)
                        persistenceManager.simulateAttrition(selectedProfile);
                }
                selectNoiseProfile(selectedProfile);
            }
        } catch (PersistenceException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Persistence Exception");
            alert.setHeaderText("Something went wrong while simulating attrition:");
            alert.setContentText(e.getCause().getMessage());
            alert.showAndWait();
        }
    }

    /**
     * shows an alert that no profile is selected
     */
    private void alertNoProfileSelected() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Track selected");
        alert.setHeaderText("Can not execute command:");
        alert.setContentText("No song selected!");
        alert.showAndWait();
    }

}
