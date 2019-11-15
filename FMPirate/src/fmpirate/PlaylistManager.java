/*
 * ---------------------------------------------------------------------------
 * File name: Playlist.java
 * Project name: FMPirate
 * Author: Ryan Haas
 * ---------------------------------------------------------------------------
 */

package fmpirate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


/**
 * This class provides the play, stop, and shuffle logic to
 * the ControllerGUI. It manages the Process playlistProcess
 * which effectively executes bash to initialize broadcast
 * and kill SoX processes as needed.
 *
 * <hr>
 *
 * @author Ryan Haas
 */
public final class PlaylistManager
{
    private static ArrayList<String> playList = new ArrayList<String>();

    private static File playlistFile;    // File object to read bash script from.
    private static Scanner fileScanner;  // To scan file contents.

    private static String tempPath = ""; // Path to temp file.
    private static String textLine;      // To store each nextLine from playlist file.

    private static Process playlistProcess;

    /**
     * Sets the content of tempPath to the string passed and calls
     * play() to attempt to bash-execute the playlist file specified
     * by tempPath using playlistProcess.
     *
     * <hr>
     * @param playListPath The path of the playlist to be executed.
     * @throws Exception
     */
    protected static void play(String playListPath) throws Exception
    {
        tempPath = playListPath;
        play();
    }

    /**
     * Attempts to bash-execute the playlist file specified
     * by tempPath using playlistProcess.
     *
     * <hr>
     * @throws Exception
     */
    protected static void play() throws Exception
    {
        playlistProcess = Runtime.getRuntime( ).exec("bash " + tempPath);
    }

    /**
     * Attempts to destroy the current playlistProcess
     * and attempts to then kill SoX (which will otherwise
     * continue to run) using playlistProcess.
     *
     * <hr>
     * @throws Exception
     */
    protected static void stop() throws Exception
    {
        playlistProcess.destroy( );
        // I have learned that actually killing SoX seems to be best way to ensure that multiple
        // SoX processes don't continue running over top of each other.
        playlistProcess = Runtime.getRuntime( ).exec("killall sox");
    }

    /**
     * Reads a bash-executable playlist and replaces the frequency
     * in each line with the new frequency passed to the method and
     * stores each non-blank line to the String ArrayList, playList.
     * Finally, createTempFile is called to make use of the ArrayList
     * contents.
     *
     * <hr>
     * @param filePath The path to a bash script in "../.temp_files"
     * @param newFrequency The frequency which was captured from the JSliders in ControllerGUI.
     * @throws IOException
     */
    protected static void shuffle(String filePath, String newFrequency) throws IOException
    { // Always update the frequency in case the user has changed the sliders before stopping current process.
        playlistFile = new File(filePath);
        fileScanner = new Scanner(playlistFile);

        while (fileScanner.hasNext())
        {
            textLine = fileScanner.nextLine()
                    .replaceAll("[0-9]+[0-9]\\.[0-9]+", newFrequency);

            if (textLine.length() > 0)
                playList.add(textLine); // Add all songs to execute.
        }

        createTempFile();
    }

    /**
     * The playList ArrayList is utilized along with the Random
     * class to randomly print the each String from the list one
     * time. This new playlist script is a shuffled version of
     * the previous script in "../.temp_files" and may be
     * executed in playListProcess.
     *
     * <hr>
     * @throws IOException
     */
    private static void createTempFile() throws IOException
    {
        Random random = new Random();

        File tempFile;
        int i = 0;
        do
        {
            tempPath = "../.temp_files/temp_file" + i;
            tempFile = new File(tempPath);
            i++;
        }while (tempFile.exists());

        FileWriter fw = new FileWriter(tempFile);
        PrintWriter pw = new PrintWriter(fw);
        int rand = 0;

        pw.println(playList.get(0)); // Print shebang header.
        pw.println(playList.get(1)); // Print "cd /home/pi/fm"
        playList.remove(0); // Remove shebang header and cd statement
        playList.remove(0); // so they don't get shuffled as songs.

        int j = 0;
        while (playList.size() > 0)
        {
            rand = random.nextInt(playList.size()); // Get random number in range.
            pw.println(playList.get(rand));			// Print String stored in random arraylist index.
            playList.remove(rand);					// Remove String of previously printed index.
            System.out.println("Printed song " + j);
            j++;
        }
        pw.close();
        playList.clear();
    } // End createTempFile method.

    protected static String getPath() {return tempPath;}
}
