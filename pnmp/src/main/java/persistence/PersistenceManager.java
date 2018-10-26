package persistence;

import exceptions.PersistenceException;
import io.jsondb.JsonDBTemplate;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import persistence.model.NoiseProfile;
import persistence.model.Scratch;
import service.ServiceProvider;
import service.util.DirectoryParser;

import javax.sound.sampled.*;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Class to access and edit the database
 * DAO for the noiseProfiles
 * also provides the function to simulate attrition of songs
 */
public class PersistenceManager {

    NoiseProfile profile;
    JsonDBTemplate jsonDBTemplate;

    /**
     * creates a JsonDBTemplate to access the database
     * @param dbFilesLocation path of the database directory
     */
    public PersistenceManager(String dbFilesLocation) {

        //Java package name where POJO's are present
        String baseScanPackage = "persistence.model";

        jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);
        if (!jsonDBTemplate.getCollectionNames().contains("pn_profiles"))
            jsonDBTemplate.createCollection(NoiseProfile.class);
    }

    /**
     * loads a file from the database and sets it as "active profile"
     * @param id id of the noiseprofile
     * @throws PersistenceException if the length of the profile could not be calculated
     */
    public void loadProfileFromDatabase(String id) throws PersistenceException{
        profile = getProfile(id);
    }

    /**
     * reloads the active {@link NoiseProfile} and returns it
     * @return the active noiseProfile
     * @throws PersistenceException if the length of the profile could not be calculated
     */
    public NoiseProfile getProfile() throws PersistenceException{
        loadProfileFromDatabase(profile.getId());
        return this.profile;
    }

    /**
     * returns the {@link NoiseProfile} searched by the given id
     * @param id the noiseprofile id
     * @return the noiseprofile
     * @throws PersistenceException if the length of the profile could not be calculated
     */
    public NoiseProfile getProfile(String id) throws PersistenceException{
        NoiseProfile noiseProfile = jsonDBTemplate.findById(id,NoiseProfile.class);
        /* create a new profile if not found */
        if (noiseProfile == null) {
            noiseProfile = new NoiseProfile(id);
            noiseProfile.setLenght(Math.max(calculateLength(id)-50,0));//set lenght to an approximate value
            noiseProfile.setTimesPlayed(0);
            jsonDBTemplate.insert(noiseProfile);
        }
        return noiseProfile;
    }

    /**
     * sets the constantNoise filed of the active {@link NoiseProfile} and persists it
     * @param name name of the constantNoise file
     */
    public void setConstantNoise(String name){
        if (profile == null) return;
        profile.setConstantNoise(name);
        jsonDBTemplate.upsert(profile);
    }

    /**
     * sets the amplifier filed of the active {@link NoiseProfile} and persists it
     * @param val new amplifier value
     */
    public void setAmplifier(int val){
        if (profile == null) return;
        profile.setAmplifier(val);
        jsonDBTemplate.upsert(profile);
    }

    /**
     * adds a new {@link Scratch} to the active {@link NoiseProfile} and persists it
     * @param scratch new scratch object
     */
    public void addScratch(Scratch scratch){
        if (profile == null) return;
        profile.getScratches().add(scratch);
        jsonDBTemplate.upsert(profile);
    }

    /**
     * removes a {@link Scratch} from the active {@link NoiseProfile} and persists the changes
     * @param scratch scratch which should be removed
     */
    public void removeScratch(Scratch scratch){
        if (profile == null) return;
        profile.getScratches().remove(scratch);
        jsonDBTemplate.upsert(profile);
    }

    /**
     * clears all noise information of the active {@link NoiseProfile} and persists the changes
     */
    public void clearProfile(){
        if (profile == null) return;
        profile.setConstantNoise("none");
        profile.getScratches().removeAll(profile.getScratches());
        profile.setAmplifier(0);
        profile.setTimesPlayed(0);
        jsonDBTemplate.upsert(profile);
    }

    /**
     * searches the {@link NoiseProfile} by the given profile-id and sets its length
     * @param id the id of the noiseprofile
     * @param length the new length
     */
    public void setLengthOfFile(String id, int length) {
        NoiseProfile noiseProfile = jsonDBTemplate.findById(id, NoiseProfile.class);
        /* create new profile if not found */
        if (noiseProfile == null) {
            noiseProfile = new NoiseProfile();
            noiseProfile.setTimesPlayed(0);
        }
        noiseProfile.setLenght(length);
        jsonDBTemplate.upsert(noiseProfile);
    }

    /**
     * simulates the attrition of the currently loaded {@link NoiseProfile}
     * randomly adds new scratches and constantNoise
     * after a number of times played (or more exact number of attrition-simulations) defined in the config the amplifier for the noises is increased
     * @param id id of the noiseprofile
     * @throws PersistenceException if the length of the profile could not be calculated
     */
    public void simulateAttrition(String id) throws PersistenceException{
        NoiseProfile noiseProfile = jsonDBTemplate.findById(id, NoiseProfile.class);
        if (noiseProfile == null) {
            noiseProfile = new NoiseProfile(id);
            noiseProfile.setTimesPlayed(0);
            noiseProfile.setAmplifier(0);
            noiseProfile.setLenght(calculateLength(id)-50);
        }
        double p_new_scratch = ServiceProvider.getPreferenceManager().getPNewScratch();
        double p_add_constantNoise = ServiceProvider.getPreferenceManager().getPNewConstNoise();
        int incAmp = ServiceProvider.getPreferenceManager().getIncAmpAfterTimesPlayed();

        int timesPlayed = noiseProfile.getTimesPlayed()+1;
        noiseProfile.setTimesPlayed(timesPlayed);


        //randomly add a new scratch
        if (Math.random() < p_new_scratch) {
            //load new scratch
            List<String> scratches = DirectoryParser.getFileList("/scratches");
            String newScratch = scratches.get(Math.min(scratches.size()-1,(int)(scratches.size() * Math.random())));
            int startFrame = (int) (Math.random() * noiseProfile.getLenght());
            Scratch scratch = new Scratch(newScratch,startFrame);
            noiseProfile.getScratches().add(scratch);
        }

        //increase the amplifier for the noise every incAmp-times played
        if (timesPlayed == incAmp) {
            noiseProfile.setTimesPlayed(0);
            noiseProfile.setAmplifier(noiseProfile.getAmplifier()+1);
        }

        //randomly add a constant noise, if there is no constant noise yet
        if (Math.random() < p_add_constantNoise && (noiseProfile.getConstantNoise() == null || ("none").equals(noiseProfile.getConstantNoise()))) {
            List<String> permanentNoises = DirectoryParser.getFileList("/permanentNoises");
            String newNoise = permanentNoises.get(Math.min(permanentNoises.size()-1,(int)(permanentNoises.size() * Math.random())));
            noiseProfile.setConstantNoise(newNoise);
            if (noiseProfile.getAmplifier() == 0)
                noiseProfile.setAmplifier(1);
        }
        jsonDBTemplate.upsert(noiseProfile);

    }

    /**
     * calculates an approximate value for the number of iterations(loading data from input to outputstream) the {@link service.playback.PlayerThread} needs
     * this approximation is calculated like this:
     * apx_length = (duration_in_seconds * sampleRate * (samplesizeinbits/8) * channels) / BUFFER_SIZE(=4096)
     *
     * @param path path of the audiofile
     * @return approximate length
     * @throws PersistenceException if the result could not be calculated
     */
    private int calculateLength(String path) throws PersistenceException{

        /* retrieve file infomration */
        File soundFile = new File(path);
        AudioInputStream buf = null;
        int duration = 0;
        try {
            buf = AudioSystem.getAudioInputStream(soundFile);

            AudioFormat format = buf.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false);


            AudioFileFormat baseFileFormat = null;
            baseFileFormat = AudioSystem.getAudioFileFormat(soundFile);
            // TAudioFileFormat properties
            if (baseFileFormat instanceof TAudioFileFormat) {
                Map properties = ((TAudioFileFormat) baseFileFormat).properties();
                String key = "duration";
                Long val = (Long) properties.get(key);
                val = val / 1000000;
                duration = val.intValue();
                /* approximate length value */
                return (int) ((duration * decodedFormat.getSampleRate() * (decodedFormat.getSampleSizeInBits() / 8) * decodedFormat.getChannels()) / 4096); //approximate number of frames
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
        return -1;

    }

}
