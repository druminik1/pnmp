package service.playback;

import exceptions.PlaybackException;
import service.ServiceProvider;

import javax.sound.sampled.*;
import java.io.File;

/**
 * class which sets up the streams needed to play audio files
 * creates a {@link PlayerThread} to then actually play the audio files
 *
 * @author Dominik Scheffknecht 1426857
 */
public class AudioPlayer {

    FloatControl volumeControl;
    AudioInputStream inputStream;
    SourceDataLine outputLine;
    PlayerThread player;


    /**
     * sets up the streams needed to playback audio:
     * A decoded {@link AudioInputStream} to read the information from the audio file and a {@link SourceDataLine} for writing data to the output-device
     * Also creates a new {@link NoiseProvider} which loads all noise samples
     * Creates a new {@link PlayerThread} with the streams, and {@link NoiseProvider} created
     *
     * @param filePath path of the audio file / song
     * @throws PlaybackException  if something went wrong setting up the PlayerThread
     */
    public void setUp(String filePath) throws PlaybackException {

        try {
            /* create new noiseprovider and pass the noiseProfile */
            NoiseProvider provider = new NoiseProvider();
            provider.loadNoiseFromProfile(ServiceProvider.getPersistenceManager().getProfile(filePath));
            ServiceProvider.getPersistenceManager().simulateAttrition(filePath);

            /* create the AudioInputStream */
            File soundFile = new File(filePath);
            AudioInputStream buf = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = buf.getFormat();
            /* decoded stream needed for MP3, no effect for wav */
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false);
            inputStream = AudioSystem.getAudioInputStream(decodedFormat, buf);

            /* get the outputline */
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            outputLine = getLine(decodedFormat);

            /* set the volume */
            this.volumeControl = (FloatControl) outputLine.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(1);

            /* create new playerthread */
            player = new PlayerThread(inputStream,outputLine,provider,filePath);

        } catch (Exception e) {
            throw new PlaybackException(e);
        }
    }

    /**
     * starts the player thread
     */
    public void play() {
        player.start();
    }

    /**
     * stops the player
     */
    public void stop() {
        if (player != null)
            player.stopPlayback();
    }

    /**
     * gets the outputline
     * @param audioFormat audioformat of the audio file
     * @return SourceDataLine used as outputline
     * @throws LineUnavailableException if no outputline could be created (e.g. if the format is not supported)
     */
    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
    {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }


    /**
     * changes the volume settings
     * @param gain new param for the gain
     */
    public void setVolume(double gain){
        if(gain != 0){
            float db = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
            this.volumeControl.setValue(db);
        } else {
            volumeControl.setValue(volumeControl.getMinimum());
        }

    }





}
