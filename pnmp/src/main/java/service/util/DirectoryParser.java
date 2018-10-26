package service.util;

import sun.misc.Launcher;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * util class to list all files contained in a directory
 * can handle directories packaged in a .jar file
 * code taken from stackoverflow:
 * https://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
 */
public class DirectoryParser {



    public static List<String> getFileList(String path) {
        final File jarFile = new File(DirectoryParser.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        ArrayList<String> files = new ArrayList<>();

        try {
        if(jarFile.isFile()) {  // Run with JAR file
            if (path.startsWith("/"))
                path = path.substring(1);
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            while(entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith(path + "/") && !name.equals(path+"/")) { //filter according to the path
                    files.add(name.substring(path.length()+1));
                }
            }
            jar.close();
        } else { // Run with IDE
            final URL url = Launcher.class.getResource(path);
            if (url != null) {
                try {
                    final File apps = new File(url.toURI());
                    for (File app : apps.listFiles()) {
                        files.add(app.getName());
                    }

                } catch (URISyntaxException ex) {
                    // never happens
                }
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }





}
