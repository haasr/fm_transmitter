/*
 * ---------------------------------------------------------------------------
 * File name: Playlist.java
 * Project name: FMPirate
 * Author: Ryan Haas
 * ---------------------------------------------------------------------------
 */

package fmpirate;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Class which reads a text file and parses the name of the WAV
 * file and the gain to be applied when broadcasting that file
 * and prints a bash-executable script which can play each audio
 * file with the specified gain over the FM frequency passed to
 * the class constructor.
 *
 * <hr>
 *
 * @author Ryan Haas
 */
public class Playlist
{
	private String audioPath = "";// Path to WAV files.
	private String tempPath = ""; // Path to temp file.
	private File pathFile;		  // File to read path from.
	private File tempFile;        // File object to write bash script to.
	private File inputFile;       // Playlist text file to read from.
	private Scanner fileScanner;  // To scan file contents.
	private PrintWriter pWriter;  // To print to temp tile/bash script.
	private String textLine;      // To store each nextLine in the text file.

	/**
	 * The constructor creates a bash-executable script in
	 * "../.temp_files". It reads from a file in "../Playlists/"
	 * (accepted from the filePath param).
	 *
	 * <hr>
	 *
	 * @param filePath  String specifying the path of a playlist text file in "../Playlists".
	 * @param frequency String specifying the frequency which will be printed to the temp_file script.
	 *
	 * @throws IOException
	 */
	public Playlist (String filePath, String frequency) throws IOException
	{
		readPath();
		makePlayList(filePath, frequency);
	}

	/**
	 * Reads the file path where all audio files should be
	 * located and stores this path to the String, audioPath.
	 */
	private void readPath() throws IOException
	{
		pathFile = new File("../.path/path.txt");
		fileScanner = new Scanner(pathFile);
		audioPath = fileScanner.nextLine();
	}

	/**
	 * Creates the bash executable playlist by parsing in each audio file name
	 * and the specified gain and converting each line from the playlist text
	 * file into a line that can be executed by bash using SoX.
	 *
	 * @param filePath The path of the playlist file being read from.
	 * @param frequency The frequency in the form,"[0-9][0-9].[.0-9]" (i.e. "102.1")".
	 * @throws IOException
	 */
	private void makePlayList (String filePath, String frequency) throws IOException
	{
		// Generate a new/unique temp file:
		int i = 0;
		do
		{
			tempPath = "../.temp_files/temp_file" + i;
			tempFile = new File(tempPath);
			i++;
		} while (tempFile.exists( ));

		pWriter = new PrintWriter(tempPath);

		// Always prints shebang header, cd to correct dir.
		pWriter.println("#! /bin/bash -");
		pWriter.println("cd " + audioPath);

		inputFile = new File(filePath);
		fileScanner = new Scanner(inputFile);

		int j = 0;
		while (fileScanner.hasNext( ))
		{
			j++;

			textLine = fileScanner.nextLine( ).replaceAll("'", "\\\\'");

			if (textLine.trim( ).startsWith("#") || textLine.length( ) == 0) // Skip over comments and blank lines.
			{
				continue;
			}

			else
			{
				String[] contents = textLine.split("\\|", 2);
				pWriter.println("sox " + contents[0]
						.replaceAll(" ", "\\\\ ") // Put an escape slash in front of each space.
						+ " -r 22050 -c 1 -b 16 -t wav - "
						+ contents[1] + " | sudo ./fm_transmitter -f " // contents[1] == gain.
						+ frequency + " - ");
				System.out.println("Printed song " + j);
			}
		}
		pWriter.close( );
	}

	/**
	 * Why doesn't java just have getter/setter properties by now?
	 */
	public String getPath ( )
	{
		return tempPath;
	}

}
