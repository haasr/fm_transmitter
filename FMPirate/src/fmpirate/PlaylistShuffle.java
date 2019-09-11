/*
 * ---------------------------------------------------------------------------
 * File name: PlaylistShuffle.java
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
 * The PlaylistShuffle class reads a bash-executable playlist
 * script in "../.temp_files" and creates a new script in
 * "../.temp_files" which randomly orders the lines specifying
 * the audio files
 */
public final class PlaylistShuffle
{	
	private static ArrayList<String> playList = new ArrayList<String>();
	private static String tempPath = ""; // Path to temp file.
	private static File playlistFile;    // File object to read bash script from.
	private static Scanner fileScanner;  // To scan file contents.
	private static String textLine;      // To store each nextLine from playlist file.

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
	public static void shuffle(String filePath, String newFrequency) throws IOException
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
	 * executed by ControllerGUI.
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

	public static String getPath(){return tempPath;}
}
