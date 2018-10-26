package persistence.model;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * dom-object representing a noiseprofile
 * this object can be mapped to the database as json-entry
 * fields:
 * <ul>
 *     <li>id - the path of the audio file</li>
 *     <li>constantNoise - name of the constantNoiseFile</li>
 *     <li>amplifier - int value of the noise amplifier</li>
 *     <li>scratches - list of scratches(dom-objects)</li>
 *     <li>length - number of frames that the playerThread reads until finished</li>
 *     <li>timesPlayed - counter of how many times the song has been played</li>
 * </ul>
 *
 * @author Dominik Scheffknecht 1426857
 */
@Document(collection = "pn_profiles", schemaVersion = "1.0")
public class NoiseProfile {

    @Id
    private String id;
    private String constantNoise;
    private int amplifier;
    private List<Scratch> scratches = new ArrayList<>();
    private int lenght;
    private int timesPlayed;

    public NoiseProfile(){

    }

    public NoiseProfile(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConstantNoise() {
        return constantNoise;
    }

    public void setConstantNoise(String constantNoise) {
        this.constantNoise = constantNoise;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public List<Scratch> getScratches() {
        return scratches;
    }

    public void setScratches(List<Scratch> scratches) {
        this.scratches = scratches;
    }

    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public void setTimesPlayed(int timesPlayed) {
        this.timesPlayed = timesPlayed;
    }
}
