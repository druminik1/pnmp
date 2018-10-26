package persistence.model;

/**
 * dom-object representing a scratch
 * this object can be mapped to the database as json-entry
 * fields:
 * <ul>
 *     <li>fileName - name of the scratch file</li>
 *     <li>startFrame - number of frames read from the playerThread after this scratch should be added</li>
 *
 * </ul>
 *
 * @author Dominik Scheffknecht 1426857
 */
public class Scratch {

    private String fileName;
    private int startFrame;

    public Scratch(){

    }

    public Scratch(String path, int startFrame) {
        this.setFileName(path);
        this.setStartFrame(startFrame);
    }



    public void setFileName(String path) {
        if(path.lastIndexOf("\\") != -1 && path.lastIndexOf("\\") != 0) {
            this.fileName = path.substring(path.lastIndexOf("\\") + 1);
        } else {
            this.fileName = path;
        }
    }

    public int getStartFrame() {
        return startFrame;
    }

    public void setStartFrame(int startFrame) {
        this.startFrame = startFrame;
    }

    public String getFileName() {
        return fileName;
    }
}
