package service.util;

import java.util.ArrayList;
import java.util.List;

/**
 * ring buffer which is used to manage the order of all tracks
 *
 * @author Dominik Scheffknecht 1426857
 */
public class TrackManager {

    private List<String> tracks = new ArrayList<>();

    public void addTrack(String path) {
        tracks.add(path);
    }

    /**
     * returns of the audio file which should be played after the given audio file
     * @param path path of the file actually playing
     * @return next audio file path
     */
    public String getNextTrack(String path) {
        int index = tracks.indexOf(path) + 1;

        /*if on en take the first file instead */
        if (index == tracks.size())
            index = 0;
        return tracks.get(index);
    }

}
