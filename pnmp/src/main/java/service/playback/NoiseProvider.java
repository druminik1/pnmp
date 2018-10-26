package service.playback;

import exceptions.NoiseProviderException;
import persistence.model.NoiseProfile;
import persistence.model.Scratch;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static service.playback.PlayerThread.BUFFER_SIZE;

/**
 * Provides all noise-data needed while playing back audio
 * there are two different methods used to save the data
 * regardless of the noise-type the audiodata is split into 4096 byte frames
 * this is the size of the buffer the {@link PlayerThread} reads audiodata from the audiofile
 * so if noise should be added, each field of the noise and audio frame/arrays can be added and returned
 * for the constant noise a list of frames is provided which is iterated in a loop until the song ends
 * for the scratch noises a hashmap is provided, the key in this map are the framenumber the noise should be played at
 * when processing the next frame, the {@link PlayerThread} always tells the {@link NoiseProvider} on which number of frame he is
 *
 * @author Dominik Scheffknecht 1426857
 */
public class NoiseProvider {

    private List<byte[]> constantNoise = new ArrayList<byte[]>();
    private Map<Integer, byte[]> scratches = new HashMap<Integer, byte[]>();
    private int amplifier;


    /**
     * loads all noisedata specified in the {@link NoiseProfile}
     * @param profile the noiseprofile
     * @throws NoiseProviderException if the noises could not be loaded
     */
    public void loadNoiseFromProfile(NoiseProfile profile) throws NoiseProviderException{
        this.amplifier = profile.getAmplifier();
        if (amplifier == 0)
            this.amplifier = 1;
        if (profile.getConstantNoise() != null && !("none").equals(profile.getConstantNoise()))
            loadConstantNoise(profile.getConstantNoise());
        for (Scratch scratch : profile.getScratches()) {
            loadScratch(scratch.getFileName(), scratch.getStartFrame());
        }
    }

    /**
     * loads the constant noise data
     * @param filename name of the noise-file
     * @throws NoiseProviderException if the noise could not be loaded
     */
    public void loadConstantNoise(String filename) throws NoiseProviderException {
        String path =  "/permanentNoises/" + filename;
        BufferedInputStream soundFileScratch = new BufferedInputStream(getClass().getResourceAsStream(path));
        /* open stream to the noise sample */
        readConstantNoise(soundFileScratch);
    }

    /**
     * reads constant noise data from the given stream and saves it to the constant noise list
     * @param soundFileScratch the stream to the noise
     * @throws NoiseProviderException if the sample could not be loaded
     */
    private void readConstantNoise(BufferedInputStream soundFileScratch) throws NoiseProviderException {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(soundFileScratch);
            AudioFormat format = stream.getFormat();

            int nBytesRead = 0;
            /* read sample and save the results into the list */
            while (nBytesRead != -1) {
                byte[] data = new byte[BUFFER_SIZE];
                nBytesRead = stream.read(data, 0, data.length);
                constantNoise.add(data);
            }
        } catch (Exception e) {
            throw new NoiseProviderException(e);
        }
    }

    /**
     * loads the scratch noise sample and saves it to the map with increasing keys starting with the given startframe
     * @param filename name of the scratch sample
     * @param startFrame startframe of the scratch
     * @throws NoiseProviderException if the noise sample could not be loaded
     */
    public void loadScratch(String filename, int startFrame) throws NoiseProviderException {
        String path =  "/scratches/" + filename;
        BufferedInputStream soundFileScratch = new BufferedInputStream(getClass().getResourceAsStream(path));
        try {
            /* open stream to the noise sample */
            AudioInputStream stream = AudioSystem.getAudioInputStream(soundFileScratch);
            AudioFormat format = stream.getFormat();

            int nBytesRead = 0;
            int i = 0;
            while (nBytesRead != -1) {
                byte[] data = new byte[BUFFER_SIZE];
                nBytesRead = stream.read(data, 0, data.length);
                if (scratches.containsKey(startFrame+i)) {
                    scratches.put(startFrame+i,combine(data, scratches.get(startFrame+i)));
                }
                scratches.put(startFrame+i,data);
                i++;
            }
        } catch (Exception e) {
            throw new NoiseProviderException(e);
        }
    }

    /**
     * adds noisedata to the raw audio given if existing
     * @param sound raw audio data
     * @param frame frame played
     * @return processed audio data (with added noises)
     */
    public byte[] processNextFrame(byte[] sound, int frame){
        if (sound == null)
            return constantNoise.get(frame%constantNoise.size());

        if (constantNoise.isEmpty() && !scratches.containsKey(frame)){
            return sound;
        } else if (!constantNoise.isEmpty() && !scratches.containsKey(frame)){
            return addConstantNoise(sound, frame);
        } else if(constantNoise.isEmpty()){
            return addScratch(sound, frame);
        } else {
            return addScratch(addConstantNoise(sound,frame),frame);
        }
    }

    /**
     * adds a constant noise to the passed sound frame
     * @param sound the raw audiodata
     * @param frame frame played
     * @return combined audio data
     */
    private byte[] addConstantNoise(byte[] sound, int frame) {
        byte[] combinded = new byte[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++){
            int sample = sound[i] + amplifier*constantNoise.get(frame%constantNoise.size())[i];
            sample = Math.max(sample, Byte.MIN_VALUE);
            sample = Math.min(sample, Byte.MAX_VALUE);
            combinded[i] = (byte) sample;
        }
        return combinded;
    }

    /**
     * adds a scratch noise to the passed sound frame
     * @param sound the raw audiodata
     * @param frame frame played
     * @return combined audio data
     */
    private byte[] addScratch(byte[] sound, int frame) {
        System.out.println("Frame: " + frame);
        byte[] combinded = new byte[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++){
            int sample = sound[i]/amplifier + amplifier * scratches.get(frame)[i];
            sample = Math.max(sample, Byte.MIN_VALUE);
            sample = Math.min(sample, Byte.MAX_VALUE);
            combinded[i] = (byte) sample;
        }
        return combinded;
    }

    /**
     * combines to audioframes by adding each field of the array with the corresponding field of the other array
     * @param data array 1
     * @param bdata array 2
     * @return combined array
     */
    private byte[] combine(byte[] data, byte[] bdata ){
        byte[] combinded = new byte[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++){
            int sample = data[i] +bdata[i];
            sample = Math.max(sample, Byte.MIN_VALUE);
            sample = Math.min(sample, Byte.MAX_VALUE);
            combinded[i] = (byte) sample;
        }
        return combinded;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    /**
     * loads constant noise form resource directory
     * @param filename name of the noise file
     * @throws NoiseProviderException  if the noise sample could not be loaded
     */
    public void loadConstantNoiseFromResource(String filename) throws NoiseProviderException {
        String path =  "/"+filename;
        BufferedInputStream soundFileScratch = new BufferedInputStream(getClass().getResourceAsStream(path));
        readConstantNoise(soundFileScratch);
    }

    public int getAmplifier() {
        return amplifier;
    }
}
