package service.util;

import java.util.prefs.Preferences;

/**
 * saves user setting with the Preference API
 * List of setting:
 * <ul>
 *     <li>libraryroot - root directory of the music library</li>
 *     <li>pNewScratch - probability of creation of a new scratch when simulating attrition</li>
 *     <li>pNewScratch - probability of creation of a new constant noise if not existing when simulation attrition</li>
 *     <li>dbpath - path to the directory the database should be placed</li>
 *     <li>incAmpAfterTimesPlayed - times played after the noise-amplifier should be increased</li>
 *     <li>degreeOfWear - specifies the degreeOfWear with a predefined or custom profile</li>
 *     <li>pathsSetYet - true if dbpath and libraryroot have been set (validated on startup)</li>
 * </ul>
 *
 * @author Dominik Scheffknecht 1426857
 */
public class PreferenceManager {

    private Preferences preferences;
    private final String LROOT = "libraryroot";
    private final String NEWSC = "pNewScratch";
    private final String NCONST = "pNewConstantNoise";
    private final String DBPATH = "dbpath";
    private final String INCAMP = "incAmpAfterTimesPlayed";
    private final String DEGREEOW = "degreeOfWear";
    private final String SET = "pathsSetYet";



    public PreferenceManager() {
        preferences = Preferences.userRoot().node(this.getClass().getName());
    }

    public void setLibraryRoot(String path) {
        preferences.put(LROOT,path);
    }

    public void setDbPath(String path) {
        preferences.put(DBPATH, path);
    }

    public void setPNewScratch(double p) {
        preferences.putDouble(NEWSC,p);
    }

    public void setPNewConstNoise(double p) {
        preferences.putDouble(NCONST,p);
    }

    public void setIncAmpAfterTimesPlayed(int val) {
        preferences.putInt(INCAMP,val);
    }

    public void setDegreeOfWear(String val) {
        preferences.put(DEGREEOW,val);
    }

    public void setSet(boolean bool) {
        preferences.putBoolean(SET,bool);
    }

    public String getLibraryRoot() {
        return preferences.get(LROOT,"notSpecified");
    }

    public String getDbPath() {
        return preferences.get(DBPATH, "notSpecified");
    }

    public double getPNewScratch() {
        return preferences.getDouble(NEWSC,0.1);
    }

    public double getPNewConstNoise() {
        return preferences.getDouble(NCONST,0.05);
    }

    public int getIncAmpAfterTimesPlayed() {
        return preferences.getInt(INCAMP,50);
    }

    public String getDegreeOfWear() {
       return preferences.get(DEGREEOW,"medium");
    }

    public boolean getSet() {
        return preferences.getBoolean(SET,false);
    }

}
