package service.playback;

import service.ServiceProvider;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

/**
 * Thread which is playing a audiofile
 * before terminating invokes playback of the next track
 *
 * @author Dominik Scheffknecht 1426857
 */
public class PlayerThread extends Thread {

    static int BUFFER_SIZE = 4096;
    AudioInputStream inputStream;
    SourceDataLine outputLine;
    NoiseProvider provider;
    String filepath;
    boolean stop = false;

    /**
     * constructor for the PlayerThread
     * @param inputStream the InputStream of the audio file
     * @param outputLine the SourceDataline audio is written to and then transported to the output device
     * @param provider the NoiseProvider prepared for the played song
     * @param filepath the path of the played song
     */
    public PlayerThread(AudioInputStream inputStream, SourceDataLine outputLine, NoiseProvider provider, String filepath) {
        this.inputStream = inputStream;
        this.outputLine = outputLine;
        this.provider = provider;
        this.filepath = filepath;
    }

    /**
     * first plays the typical "needle-down" noise heard when a record player starts playing
     * then plays the song by repellingly reading audiodata from the inputstream and writing to the outputline
     * between this two steps, the audiodata is processed by the {@link NoiseProvider}, there the noise is added
     * after the song was played completely the {@link AudioPlayer} is set up and started with the next song received from the {@link service.util.TrackManager}
     */
    @Override
    public void run() {
        int nBytesRead = 0;

        int c = 0;
        byte[] abData = new byte[BUFFER_SIZE];
        try {
            outputLine.start();
            //playing needle down noise
            for(int i = 0; i<123; i++) {
                ServiceProvider.getNeedleDownNoiseProvider().setAmplifier(provider.getAmplifier());
                outputLine.write(ServiceProvider.getNeedleDownNoiseProvider().processNextFrame(abData,i),0,BUFFER_SIZE);
            }
            //playing track
            while (nBytesRead != -1 && !stop) {
                nBytesRead = inputStream.read(abData, 0, abData.length);
                c++;

                if (nBytesRead >= 0) {
                    outputLine.write(provider.processNextFrame(abData,c),0,BUFFER_SIZE);
                } else {
                    /* finished: start next song */
                    ServiceProvider.getPersistenceManager().setLengthOfFile(filepath,c);
                    ServiceProvider.getAudioPlayer().setUp(ServiceProvider.getTrackManager().getNextTrack(filepath));
                    ServiceProvider.getAudioPlayer().play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            outputLine.drain();
            outputLine.close();
        }
    }

    /* stops the thread */
    public void stopPlayback(){
        stop = true;
    }
}
