/*
 * ---------------------------------------------------------------------------
 * File name: ControllerDriver.java
 * Project name: FMPirate
 * Author: Ryan Haas
 * ---------------------------------------------------------------------------
 */

package fmpirate;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Driver class which instantiates ControllerGUI and cleans up
 * after it by removing the the temporary files created in the ../.temp_files.
 *
 * <hr>
 * @author Ryan Haas
 */
public class ControllerDriver
{
	/**
	 * main attempts to recreate essential directories if they have been removed
	 * and instantiates an instance of ControllerGUI.
	 *
	 * <hr>
	 * @param args
	 */

	public static void main(String[] args)
	{
		Dimension SIZE = new Dimension(700, 900);

		try // Create these 2 essential directories, if they're missing.
		{
			Files.createDirectories(Paths.get("../Playlists"));
			Files.createDirectories(Paths.get("../.temp_files"));
		}
		catch (IOException ioEx) // Couldn't create missing dirs. User needs to put them back; program cannot continue.
		{
			JOptionPane.showMessageDialog(null, "The folder, \"Playlists\" or \".temp_files\"\n" +
					"is missing. Please recreate these\nfolders in the current directory.\n The program will " +
					"terminate.", "Missing Directories", JOptionPane.WARNING_MESSAGE);

			System.exit(0);
		}

		new ControllerGUI("FM PiRate - fm_transmitter frontend",
				SIZE, "../res/assets/fmpirate.png");

		deleteTempFiles(); // Clear all temp files on exit.
	} // End main method.

	/**
	 * deleteTempFiles flushes all files in the format "temp_file0",
	 * "temp_file1", etc. from "../.temp_files" which were created
	 * while ControllerGUI was alive.
	 */
	private static void deleteTempFiles()
	{
		File tempDirectory = new File("../.temp_files");
		int fileCount = tempDirectory.list().length;

		for (int i = 0; i < fileCount; i++)
		{
			try
			{
				Files.deleteIfExists(new File("../.temp_files/temp_file" + i).toPath());
			}
			catch (IOException ioEx)
			{
				System.out.println("Failed to delete temp file:");
				ioEx.printStackTrace();
			}
		} // End for.
	} // End deleteTempFiles method.
} // End class.
