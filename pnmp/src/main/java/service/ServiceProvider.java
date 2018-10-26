package service;

import exceptions.NoiseProviderException;
import persistence.PersistenceManager;
import service.playback.AudioPlayer;
import service.playback.NoiseProvider;
import service.util.TrackManager;
import service.util.PreferenceManager;

/**
 * class which provides all service classes and util classes needed over static getter methods
 * every class provided is only instantiated once
 *
 * @author Dominik Scheffknecht 1426857
 */
public class ServiceProvider {

    private static PersistenceManager persistenceManager;
    private static AudioPlayer audioPlayer;
    private static TrackManager trackManager;
    private static NoiseProvider needleDownNoiseProvider;
    private static PreferenceManager preferenceManager;

    public static PersistenceManager getPersistenceManager() {
        if (persistenceManager == null) {
            String dbPath = getPreferenceManager().getDbPath();
            persistenceManager = new PersistenceManager(dbPath);
        }

        return persistenceManager;
    }

    public static AudioPlayer getAudioPlayer() {
        if (audioPlayer == null)
            audioPlayer = new AudioPlayer();
        return audioPlayer;
    }

    public static TrackManager getTrackManager() {
        if (trackManager == null)
            trackManager = new TrackManager();
        return trackManager;
    }

    public static NoiseProvider getNeedleDownNoiseProvider() throws NoiseProviderException {
        if (needleDownNoiseProvider == null) {
            needleDownNoiseProvider = new NoiseProvider();
            needleDownNoiseProvider.loadConstantNoiseFromResource("needleDown.wav");
            needleDownNoiseProvider.setAmplifier(6);
        }
        return needleDownNoiseProvider;
    }

    public static PreferenceManager getPreferenceManager() {
        if (preferenceManager == null) {
            preferenceManager = new PreferenceManager();
        }
        return preferenceManager;
    }

}
