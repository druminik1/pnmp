package gui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import service.ServiceProvider;

import java.io.File;

/**
 * FXML controller for the musicLibrary window
 * iterates through the musiclibrary-filetree and builds the library tree
 *
 * @author Dominik Scheffknecht 1426857
 */
public class MusicLibraryController {

    PlayerController playerController;
    NoiseEditorController noiseEditorController;

    @FXML
    TreeView trv_library;

    /**
     * loads the musiclibrary and sets eventlisteners etc. on the treeView
     */
    @FXML
    public void initialize (){

        /* load and validate the music-library root directory */
        String rootPath = ServiceProvider.getPreferenceManager().getLibraryRoot();
        File rootDir = new File(rootPath);
        if (!rootDir.isDirectory())
            throw new RuntimeException("File at RootPath is no Directory");


        /* set cellFactory to only show the fileName instead of the full path */
        trv_library.setCellFactory(new Callback<TreeView, TreeCell>() {
            public TreeCell call(TreeView param) {
                return new TextFieldTreeCell<String>(new StringConverter<String>() {
                    @Override
                    public String toString(String object) {
                        if(object.lastIndexOf("\\") != -1 && object.lastIndexOf("\\") != 0) {
                            return object.substring(object.lastIndexOf("\\") + 1);
                        }
                        return object;
                    }

                    @Override
                    public String fromString(String string) {
                        return string;
                    }
                });
            }
        });

        /* fill the treeView */
        trv_library.setRoot(addFiles(rootDir));

        /* set treeView-eventhandler
         * on doubleclick play track and show it in the noiseEditor
         * on single click only show it in the noiseEditor         *
          */
        trv_library.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                TreeItem<String> itm = (TreeItem<String>) trv_library.getSelectionModel().getSelectedItem();
                if (itm == null) return;
                String filePath = itm.getValue();

                if (checkFileExtension(new File(filePath))) {
                    if(event.getClickCount() == 2){
                        playerController.playTrack(itm.getValue());
                    }
                    noiseEditorController.selectNoiseProfile(filePath);
                }
            }
        });

    }

    /**
     * creates a {@link TreeItem} representing the directory given as param
     * all files contained in the directory are added as children
     * for each subdirectory this method is called again and then the result is added as child
     *
     * @param dir rootdirectory
     * @return TreeItem representing the directory given as param
     */
    private TreeItem<String> addFiles(File dir){
        TreeItem<String> item = new TreeItem<String>(dir.toString());
        for (File file: dir.listFiles()){
            if (file.isDirectory()){
                item.getChildren().add(addFiles(file));
            } else if(checkFileExtension(file)) {
                item.getChildren().add(new TreeItem<String>(file.getAbsolutePath()));
                ServiceProvider.getTrackManager().addTrack(file.getAbsolutePath());
            }
        }
        return item;
    }

    /**
     * checks if the given file has the extension mp3 or wav
     * @param file the file to check
     * @return true if the file was an mp3 or wav
     */
    private boolean checkFileExtension(File file){
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0){
            String ext = fileName.substring(fileName.lastIndexOf(".")+1);
            if (("mp3").equals(ext) || ("wav").equals(ext))
                return true;
        }
        return false;
    }


    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
    }

    public void setNoiseEditorController(NoiseEditorController noiseEditorController) {
        this.noiseEditorController = noiseEditorController;
    }
}
