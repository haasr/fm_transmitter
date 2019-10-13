 /*
  * ---------------------------------------------------------------------------
  * File name: ControllerGUI.java
  * Project name: FMPirate
  * Author: Ryan Haas
  * ---------------------------------------------------------------------------
  */

 package fmpirate;

 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.*;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.Scanner;

 /**
  * GUI window to select a playlist file and interact with the Playlist
  * and PlaylistShuffle classes to create and shuffle bash-executable
  * WAV playlists and broadcast them over FM radio.
  * <p>
  * CREDIT ICON: // Icon from https://smashicons.com/
  *
  * <hr>
  *
  * @author Ryan Haas
  */
 public class ControllerGUI extends JFrame
 {
	 private static final long serialVersionUID = 1L;
	 private static Font MAIN_FONT = new Font("Calibre", Font.BOLD, 32);
	 private static Font FCHOOSER_FONT = new Font("Calibre", Font.BOLD, 18); // For JFileChooser.
	 private final Dimension MSG_SIZE = new Dimension(500, 300); // Size for message windows.

	 // Init colors in setColorTheme() when constructor is called:
	 Color bgColor;                         // Background color.
	 Color btnColor;                        // Color for buttons/components.
	 Color txtColor;                        // Text color.
	 Playlist playlist;                     // Playlist object to convert text file to executable file
	 // Panels:
	 // Strange things were happening to the state of the shuffle_btn.
	 // I reversed the positions of the shuffle_btn and play_btn and it
	 // happened to the play_btn but not the shuffle. I took JSliders out
	 // and still same issue. I concluded that something similar to what
	 // is described in this bug report(https://bugs.java.com/bugdatabase
	 // /view_bug.do?bug_id=4368790) occurred. My solution was just to put
	 // a panel with invisible button in the container first. This is the
	 // purpose of dummy_btn and dummy_panel.
	 private JPanel logoPanel;
	 private JPanel file_chooserPanel;
	 private JPanel sliderPanel;
	 private JPanel dummyPanel;
	 private JPanel buttonPanel;
	 private JPanel themePanel;
	 private JPanel container; // All JPanels will get added to container JPanel in order, top to bottom.
	 // Components:
	 private JFileChooser file_chooser;     // No listener.
	 private JTextField choose_file_field;  // No listener.
	 private JButton choose_file_btn;       // Add listener in setChooseFileHandler().
	 private JButton shuffle_btn;           // Add listener in setControlsHandler().
	 private JButton play_stop_btn;         // Add listener in setControlsHandler().
	 private JButton dummy_btn;             // No listener.
	 private JSlider base_slider;           // Add listener in setSliderHandler().
	 private JSlider deci_slider;           // Add listener in setSliderHandler().
	 private JToggleButton theme_toggle;    // Add listener in setToggleHandler().
	 // Labels (for sliders):
	 private JLabel baseLabel;
	 private JLabel deciLabel;
	 // JSliders:
	 // Sliders use ints only. frequency = baseValue + "." + deciValue if changed.
	 private int baseValue      = 0;        // baseValue will be from 89 and 107.
	 private int deciValue      = 0;        // deciValue will be from 0 to 9.
	 private String frequency   = "102.1";  // Initial frequency if not changed.
	 private String filePath    = "";       // Path of playlist file as selected from JFileChooser.
	                                        // which is created in .temp_files dir.
	 private Process playlistProcess;       // Process for executing script.

	 private String toggleText;             // Toggle btn text -- "Theme: Light" or "Theme: Dark".

	 private boolean fileSelected = false;  // Flag becomes true after file imported and false if
	                                        // exception is thrown trying to execute the playlist
	                                        // generate from the file.

	 private boolean playFile = false;      // Flag is toggled by the play/stop btn to execute or kill playlistProcess.
	 private boolean shuffled = false;      // Flag initially false because Playlist must create a tempFile first.
	                                        // Once the shuffle btn has been pressed (after a file is imported),
	                                        // instead of creating a tempFile using a new Playlist instance, I create a
	                                        // temp_file using PlaylistShuffle to shuffle the songs around.

	 // If there were a problem with the .theme/theme.txt file, default to light:
	 private boolean lightTheme = true;

	 /**
	  * Sets the title and size of the JFrame to be displayed, initializes
	  * and adds all content to the JFrame and registers listeners for the
	  * GUI's various components before finally making it visible.
	  *
	  * <hr>
	  *
	  * @param title    Title to be displayed on the top bar of the JFrame.
	  * @param size     Dimension object to specify the  JFrame window size.
	  * @param logoPath Path to the logo to be displayed atop the window.
	  */
	 public ControllerGUI (String title, Dimension size, String logoPath)
	 {
		 super(title);
		 setColorTheme( );                 // Read .theme/theme.txt and set light or dark theme.
		 setPreferredSize(size);

		 initPanels( );                    // Initialize all panels.
		 initComponents( );                // Initialize all the components (buttons, sliders, text field, etc.).
		 addContent(logoPath);             // Add components to panels, panels to container panel, add container to this

		 setShuffleHandler( );             // Listener to control shuffle functionality.
		 setPlayStopHandler( );            // Listener to control play/stop functionality.
		 setChooseFileHandler( );          // Listener to control the import button.
		 setSliderHandler( );              // Listeners to control the JSliders.
		 setToggleHandler( );              // Listener for the theme toggle btn.

		 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 pack( );
		 setLocationRelativeTo(null);
		 setVisible(true);
	 } // End of constructor.

	 /**
	  * Reads the theme file in "../.theme/theme.txt"  which either says
	  * "Theme: Dark" or "Theme: Light". and sets the Color objects,
	  * bgColor, btnColor, and txtColor according to the theme.
	  */
	 private void setColorTheme ( )
	 {
		 File themeFile = new File("../.theme/theme.txt");

		 try
		 {
			 Scanner fileScanner = new Scanner(themeFile);

			 if (fileScanner.nextLine( ).equals("Theme: Dark"))
			 {
				 lightTheme = false;
			 }

			 fileScanner.close( );
		 }
		 catch (Exception ex)
		 {
			 UIManager.put("Panel.background", bgColor);
			 UIManager.put("OptionPane.messageFont", MAIN_FONT);
			 UIManager.put("OptionPane.buttonFont", MAIN_FONT);
			 UIManager.put("OptionPane.messageForeground", txtColor);
			 UIManager.put("OptionPane.minimumSize", MSG_SIZE);
			 UIManager.put("OptionPane.background", btnColor);
			 UIManager.put("Panel.background", bgColor);
			 UIManager.put("Button.background", btnColor);
			 UIManager.put("Button.foreground", txtColor);

			 JOptionPane.showMessageDialog(null, "The theme could not be changed.",
					 "Error", JOptionPane.WARNING_MESSAGE);

			 System.out.println("ControllerGUI.setColorTheme exception:\n");
			 ex.printStackTrace( );
		 }

		 if (lightTheme)
		 {
			 bgColor = Color.WHITE;
			 btnColor = new Color(239, 239, 239);
			 txtColor = new Color(27, 27, 28);
		 }
		 else
		 {
			 bgColor = new Color(27, 27, 28);
			 btnColor = new Color(51, 51, 51);
			 txtColor = Color.WHITE;
		 }

	 } // End of setColorTheme method.

	 /**
	  * Initializes all the JPanels and JLabels and applies styling
	  * such as background color and layouts.
	  */
	 private void initPanels ( )
	 {
		 themePanel = new JPanel( );
		 themePanel.setBackground(bgColor);

		 logoPanel = new JPanel( );
		 logoPanel.setBackground(bgColor);

		 file_chooserPanel = new JPanel( );
		 file_chooserPanel.setBackground(bgColor);

		 sliderPanel = new JPanel( );
		 sliderPanel.setBackground(bgColor);
		 // Border to add margin space:
		 sliderPanel.setBorder(BorderFactory.createEmptyBorder(0, 64, 0, 64));
		 sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));

		 baseLabel = new JLabel("102"); // Default base frequency.
		 baseLabel.setFont(MAIN_FONT);
		 baseLabel.setForeground(txtColor);

		 deciLabel = new JLabel(".1"); // Default decimal frequency.
		 deciLabel.setFont(MAIN_FONT);
		 deciLabel.setForeground(txtColor);

		 dummyPanel = new JPanel( );
		 dummyPanel.setBackground(bgColor);

		 buttonPanel = new JPanel( );
		 buttonPanel.setBackground(bgColor);

		 container = new JPanel( );
		 container.setBackground(bgColor);
		 container.setLayout(new BoxLayout(this.container, BoxLayout.Y_AXIS));
	 } // End of initPanels method.

	 /**
	  * Initializes all components -- JTextfield, JButtons, etc. --
	  * and styles them.
	  */
	 private void initComponents ( )
	 {
		 if (lightTheme)
		 {
			 toggleText = "Theme: Light";
		 }
		 else
		 {
			 toggleText = "Theme: Dark";
		 }

		 choose_file_field = new JTextField("Choose playlist", 12);
		 choose_file_field.setFont(MAIN_FONT);
		 choose_file_field.setEditable(false);
		 choose_file_field.setForeground(txtColor);
		 choose_file_field.setBackground(btnColor);

		 choose_file_btn = new JButton("Import");
		 choose_file_btn.setFont(MAIN_FONT);
		 choose_file_btn.setForeground(txtColor);
		 choose_file_btn.setBackground(btnColor);

		 base_slider = new JSlider(0, 89, 107, 102);
		 base_slider.setFont(MAIN_FONT);
		 base_slider.setForeground(txtColor);
		 base_slider.setBackground(btnColor);
		 base_slider.setMajorTickSpacing(5);
		 base_slider.setPaintTicks(true);

		 deci_slider = new JSlider(0, 0, 9, 1);
		 deci_slider.setFont(MAIN_FONT);
		 deci_slider.setForeground(txtColor);
		 deci_slider.setBackground(btnColor);
		 deci_slider.setMajorTickSpacing(1);
		 deci_slider.setPaintTicks(true);

		 dummy_btn = new JButton( );
		 dummy_btn.setBackground(bgColor);
		 dummy_btn.setBorder(BorderFactory.createEmptyBorder( ));

		 shuffle_btn = new JButton(new ImageIcon("../res/assets/shuffle.png"));
		 shuffle_btn.setBackground(btnColor);

		 play_stop_btn = new JButton(new ImageIcon("../res/assets/stop.png"));
		 play_stop_btn.setBackground(btnColor);

		 theme_toggle = new JToggleButton(toggleText);
		 theme_toggle.setFont(MAIN_FONT);
		 theme_toggle.setForeground(txtColor);
		 theme_toggle.setBackground(btnColor);
	 } // End of initComponents method.

	 /**
	  * Adds all components (JLabels, JTextfield, JButtons, etc.)
	  * to their respective panels. All panels other panels are
	  * added to the JPanel, container which stacks them vertically
	  * (according to BoxLayout.Y_AXIS).
	  *
	  * @param logoPath Path to the logo to be displayed atop the window.
	  */
	 private void addContent (String logoPath)
	 {
		 JLabel label = new JLabel(new ImageIcon(logoPath));
		 logoPanel.add(label);

		 file_chooserPanel.add(choose_file_field);
		 file_chooserPanel.add(choose_file_btn);

		 sliderPanel.add(baseLabel);
		 sliderPanel.add(base_slider);
		 sliderPanel.add(deciLabel);
		 sliderPanel.add(deci_slider);

		 dummyPanel.add(dummy_btn);

		 buttonPanel.add(shuffle_btn);
		 buttonPanel.add(play_stop_btn);

		 themePanel.add(theme_toggle);

		 container.add(logoPanel);
		 container.add(file_chooserPanel);
		 container.add(sliderPanel);
		 container.add(new JLabel("\n"));
		 container.add(dummyPanel);
		 container.add(buttonPanel);
		 container.add(themePanel);

		 add(container);
	 } // End of addContent method.

	 private void setSliderHandler ( )
	 {
		 base_slider.addChangeListener(changeEvent -> {

			 baseValue = base_slider.getValue( );
			 deciValue = deci_slider.getValue( );
			 frequency = baseValue + "." + deciValue;
			 baseLabel.setText("" + baseValue);
			 // When a slider value changes, create a new Playlist object when playFile true.
			 // Accomplished by setting shuffled back to false.
			 shuffled = false;
		 });


		 deci_slider.addChangeListener(changeEvent -> {

			 baseValue = base_slider.getValue( );
			 deciValue = deci_slider.getValue( );
			 frequency = baseValue + "." + deciValue;
			 deciLabel.setText("." + deciValue);
			 // When a slider value changes, create a new Playlist object when playFile true.
			 // Accomplished by setting shuffled back to false.
			 shuffled = false;
		 });
	 } // End of setSliderHandler method.

	 /**
	  * Registers a listener to the JToggleButton theme_toggle
	  * to allow the user to toggle which theme should be displayed
	  * next time the program is started.
	  */
	 private void setToggleHandler ( )
	 {
		 theme_toggle.addActionListener(actionEvent -> {

			 File themeFile;
			 FileWriter fw;
			 PrintWriter pw;

			 lightTheme = lightTheme != true;

			 themeFile = new File("../.theme/theme.txt");

			 if (lightTheme)
			 {
				 toggleText = "Theme: Light";
				 theme_toggle.setText(toggleText);
				 theme_toggle.revalidate( );
			 }
			 else
			 {
				 toggleText = "Theme: Dark";
				 theme_toggle.setText(toggleText);
				 theme_toggle.revalidate( );
			 }

			 try
			 {
				 fw = new FileWriter(themeFile);
				 pw = new PrintWriter(fw);
				 pw.println(toggleText);
				 pw.close( );
			 }
			 catch (Exception ex)
			 {
				 JOptionPane.showMessageDialog(null, "The theme could not be changed.",
						 "Error", JOptionPane.WARNING_MESSAGE);

				 System.out.println("Could not edit ../.theme/theme.txt");
				 System.out.println("ControllerGUI.setHandlers/theme_toggle exception:\n");

				 ex.printStackTrace( );
			 }

			 UIManager.put("Panel.background", bgColor);
			 UIManager.put("OptionPane.messageFont", MAIN_FONT);
			 UIManager.put("OptionPane.buttonFont", MAIN_FONT);
			 UIManager.put("OptionPane.messageForeground", txtColor);
			 UIManager.put("OptionPane.minimumSize", MSG_SIZE);
			 UIManager.put("OptionPane.background", btnColor);
			 UIManager.put("Panel.background", bgColor);
			 UIManager.put("Button.background", btnColor);
			 UIManager.put("Button.foreground", txtColor);

			 JOptionPane.showMessageDialog(null,
					 "Theme will be changed the next\ntime you launch the program.",
					 "Theme Changed", JOptionPane.INFORMATION_MESSAGE);
		 });
	 } // End of setToggleHandler method.

	 /**
	  * Registers a listener to the JButton shuffle_btn. When the flag
	  * playFile is false (meaning a playlist isn't being broadcasted)
	  * and the flag fileSelected (meaning a playlist file was imported)
	  * is true, the PlaylistShuffle class randomly re-orders the
	  * playlist and re-initializes playListProcess to use bash to
	  * execute the new playlist file. When playFile is true and
	  * fileSelected is true, the current playListProcess must first
	  * be terminated before the PlaylistShuffle class is called
	  * and the playlistProcess is re-initialized to execute the
	  * new shuffled playlist.
	  */
	 private void setShuffleHandler ( )
	 {
		 shuffle_btn.addActionListener(actionEvent -> {

			 if (!playFile && fileSelected) // File is selected and ready to play/stopped.
			 {
				 try
				 {
					 Thread.sleep(1200); // Give ample time for the previous process to have been killed.
					 PlaylistShuffle.shuffle(playlist.getPath( ), frequency);
					 String tempPath = PlaylistShuffle.getPath( );
					 playlistProcess = Runtime.getRuntime( ).exec("bash " + tempPath);

					 System.out.println("\nplaylistProcess started\n");

					 playFile = true; // File is playing.
					 shuffled = true; // Will use PlaylistShuffle.getPath() to get playlist next time play btn pushed.

					 play_stop_btn.setIcon(new ImageIcon("../res/assets/play.png"));
					 play_stop_btn.revalidate( );

				 }
				 catch (Exception ex)
				 {
					 // This exception will be caught when the user has first launched
					 // the program, imported a file, and pressed shuffle_btn, since
					 // no temp files exist yet. Go ahead and create a shuffled playlist here:

					 System.out.println("Could not shuffle playlist");
					 System.out.println("ControllerGUI.setHandlers/shuffle_btn exception:\n");
					 ex.printStackTrace( );

					 try
					 {
						 playlist = new Playlist(filePath, frequency);
						 System.out.println("\n\nPlaylist path: " + playlist.getPath( ) +
								 " Frequency: " + frequency);

						 PlaylistShuffle.shuffle(playlist.getPath( ), frequency);
						 String tempPath = PlaylistShuffle.getPath( );
						 playlistProcess = Runtime.getRuntime( ).exec("bash " + tempPath);

						 System.out.println("\nplaylistProcess started\n");

						 // File is playing.
						 playFile = true;
						 // Program will use PlaylistShuffle.getPath() to get playlist next time play btn pushed.
						 shuffled = true;

						 play_stop_btn.setIcon(new ImageIcon("../res/assets/play.png"));
						 play_stop_btn.revalidate( );
					 }
					 catch (Exception e)
					 {
						 JOptionPane.showMessageDialog(null, "Could not execute the playlist file",
								 "Playlist error", JOptionPane.ERROR_MESSAGE);

						 System.out.println("Could not execute the playlist file.");
						 System.out.println("ControllerGUI.setHandlers/play_stop_btn exception:\n");
						 e.printStackTrace( );

						 fileSelected = false;
						 playFile = false;
					 }
				 }
			 } // End if.

			 else if (playFile && fileSelected) // File is playing at the moment.
			 {
				 try
				 {
					 playlistProcess.destroy( );
					 playlistProcess.waitFor( );
					 playlistProcess = Runtime.getRuntime( ).exec("sudo killall sox");

					 System.out.println("playlistProcess destroyed");
				 }
				 catch (Exception ex)
				 {
					 System.out.println("Error killing process");
					 System.out.println("ControllerGUI.setHandlers/shuffle_btn exception:\n");
					 ex.printStackTrace( );
				 }

				 try
				 {
					 Thread.sleep(1200); // This gives ample time for the previous process to have been killed.
					 PlaylistShuffle.shuffle(playlist.getPath( ), frequency);
					 String tempPath = PlaylistShuffle.getPath( );
					 playlistProcess = Runtime.getRuntime( ).exec("bash " + tempPath);

					 System.out.println("\nplaylistProcess started\n");

					 shuffled = true; // Will use PlaylistShuffle.getPath() to get playlist next time play btn pushed.

					 play_stop_btn.setIcon(new ImageIcon("../res/assets/play.png"));
					 play_stop_btn.revalidate( );

				 }
				 catch (Exception ex)
				 {

					 UIManager.put("Panel.background", bgColor);
					 UIManager.put("OptionPane.messageFont", MAIN_FONT);
					 UIManager.put("OptionPane.buttonFont", MAIN_FONT);
					 UIManager.put("OptionPane.messageForeground", txtColor);
					 UIManager.put("OptionPane.minimumSize", MSG_SIZE);
					 UIManager.put("OptionPane.background", btnColor);
					 UIManager.put("Panel.background", bgColor);
					 UIManager.put("Button.background", btnColor);
					 UIManager.put("Button.foreground", txtColor);

					 JOptionPane.showMessageDialog(null, "The file could not be shuffled",
							 "Shuffle error", JOptionPane.WARNING_MESSAGE);

					 System.out.println("Could not shuffle playlist");
					 System.out.println("ControllerGUI.setHandlers/shuffle_btn exception:\n");

					 ex.printStackTrace( );
				 }

			 } // End else if.
		 });
	 } // End setShuffleHandler method.

	 /**
	  * Registers a listener to the JButton play_stop_btn. If
	  * a file is imported (fileSelected is true), then every
	  * time the button is pressed, the flag playFile will
	  * flip. If shuffled is false (meaning PlaylistShuffle has
	  * not been used to create a shuffled playlist of the
	  * current frequency), a new Playlist object will be
	  * created and playlistProcess will execute the newly
	  * created playlist assuming playFile is true. If false,
	  * the current playListProcess will be terminated.
	  * Assuming shuffled and fileSelected are true, however,
	  * if playFile is true, the last shuffled file will
	  * be executed by re-initializing playlistProcesss; if
	  * false, the running playListProcess will be terminated.
	  */
	 private void setPlayStopHandler ( )
	 {
		 play_stop_btn.addActionListener(actionEvent -> {

			 if (!shuffled && fileSelected) // File imported and we use Playlist to create a new temp_file.
			 {
				 playFile = playFile != true;

				 if (playFile)
				 {

					 try
					 {
						 playlist = new Playlist(filePath, frequency);
						 System.out.println("\n\nPlaylist path: " + playlist.getPath( ) +
								 " Frequency: " + frequency);

						 playlistProcess = Runtime.getRuntime( ).exec("bash " + playlist.getPath( ));
						 System.out.println("\nplaylistProcess started\n");

						 play_stop_btn.setIcon(new ImageIcon("../res/assets/play.png"));
						 play_stop_btn.revalidate( );
					 }
					 catch (Exception ex)
					 {

						 UIManager.put("Panel.background", bgColor);
						 UIManager.put("OptionPane.messageFont", MAIN_FONT);
						 UIManager.put("OptionPane.buttonFont", MAIN_FONT);
						 UIManager.put("OptionPane.messageForeground", txtColor);
						 UIManager.put("OptionPane.minimumSize", MSG_SIZE);
						 UIManager.put("OptionPane.background", btnColor);
						 UIManager.put("Panel.background", bgColor);
						 UIManager.put("Button.background", btnColor);
						 UIManager.put("Button.foreground", txtColor);

						 JOptionPane.showMessageDialog(null, "Could not execute the playlist file.\n" +
								 "Please check the formatting.", "Playlist error", JOptionPane.WARNING_MESSAGE);

						 System.out.println("Could not exec Playlist object");
						 System.out.println("ControllerGUI.setHandlers/play_stop_btn exception:\n");
						 ex.printStackTrace( );

						 choose_file_field.setText("Choose playlist");
						 choose_file_field.setEditable(false);

						 fileSelected = false; // File is probably of invalid format.
						 playFile = false;
					 }

				 } // End inner if.

				 else if (!playFile)
				 {

					 try
					 {
						 playlistProcess.destroy( );
						 playlistProcess.waitFor( );
						 playlistProcess = Runtime.getRuntime( ).exec("sudo killall sox");

						 System.out.println("playlistProcess destroyed");

						 play_stop_btn.setIcon(new ImageIcon("../res/assets/stop.png"));
						 play_stop_btn.revalidate( );
					 }
					 catch (Exception ex)
					 {
						 System.out.println("Exception killing process");
						 System.out.println("ControllerGUI.setHandlers.play_stop_btn exception:\n");
						 ex.printStackTrace( );
					 }
				 } // End inner else.
			 } // End outer if.

			 else if (shuffled && fileSelected) // There is a shuffled version of temp_file to play from.
			 {
				 playFile = playFile != true;

				 if (playFile)
				 {
					 try
					 {
						 playlistProcess = Runtime.getRuntime( ).exec("bash " + PlaylistShuffle.getPath( ));
						 System.out.println("\nplaylistProcess started\n");
						 playFile = true;

						 play_stop_btn.setIcon(new ImageIcon("../res/assets/play.png"));
						 play_stop_btn.revalidate( );

					 }
					 catch (Exception ex)
					 {
						 UIManager.put("Panel.background", bgColor);
						 UIManager.put("OptionPane.messageFont", MAIN_FONT);
						 UIManager.put("OptionPane.buttonFont", MAIN_FONT);
						 UIManager.put("OptionPane.messageForeground", txtColor);
						 UIManager.put("OptionPane.minimumSize", MSG_SIZE);
						 UIManager.put("OptionPane.background", btnColor);
						 UIManager.put("Panel.background", bgColor);
						 UIManager.put("Button.background", btnColor);
						 UIManager.put("Button.foreground", txtColor);

						 JOptionPane.showMessageDialog(null, "Could not execute the playlist file",
								 "Playlist error", JOptionPane.ERROR_MESSAGE);

						 System.out.println("Could not execute the playlist file.");
						 System.out.println("ControllerGUI.setHandlers.play_stop_btn exception:\n");
						 ex.printStackTrace( );
					 }
				 } // End inner if.

				 else if (!playFile) // User stops the playlist.
				 {

					 try
					 {
						 playlistProcess.destroy( );
						 playlistProcess.waitFor( );

						 playlistProcess = Runtime.getRuntime( ).exec("sudo killall sox");

						 System.out.println("playlistProcess destroyed");

						 play_stop_btn.setIcon(new ImageIcon("../res/assets/stop.png"));
						 play_stop_btn.revalidate( );
					 }
					 catch (Exception ex)
					 {
						 System.out.println("Error killing process");
						 System.out.println("ControllerGUI.setHandlers.play_stop_btn exception:\n");
						 ex.printStackTrace( );
					 }
				 } // End inner else.
			 } // End outer else if.
		 });
	 } // End of setPlayStopHandler method.

	 /**
	  * Registers a listener for the JButton, choose_file_btn. When
	  * the button is pressed, a JFileChooser instance of file_chooser
	  * launches in the directory, "../Playlists". Once a text file
	  * is selected, its path will appear in the JTextfield,
	  * choose_file_field and the flag fileSelected becomes true.
	  */
	 private void setChooseFileHandler ( )
	 {
		 choose_file_btn.addActionListener(actionEvent -> {

			 UIManager.put("ScrollPane.background", bgColor);
			 UIManager.put("List.background", bgColor);
			 UIManager.put("List.foreground", txtColor);
			 UIManager.put("Panel.background", bgColor);
			 UIManager.put("Button.background", btnColor);
			 UIManager.put("Button.foreground", txtColor);
			 UIManager.put("Textfield.background", btnColor);
			 UIManager.put("Textfield.foreground", txtColor);

			 file_chooser = new JFileChooser("../Playlists");
			 setFileChooserFont(file_chooser.getComponents( ));
			 FileNameExtensionFilter txtFilter = new FileNameExtensionFilter
					 ("Text files", "txt", "text");
			 file_chooser.setFileFilter(txtFilter);
			 file_chooser.setDialogTitle("Choose a text file to import");
			 file_chooser.setApproveButtonToolTipText("Select the file you want to import, then click me.");

			 int option = file_chooser.showOpenDialog(null);
			 if (option == JFileChooser.APPROVE_OPTION)
			 {
				 shuffled = false;
				 filePath = file_chooser.getSelectedFile( ).toString( );
				 fileSelected = true;
				 choose_file_field.setText(filePath);
				 choose_file_field.setEditable(false);
			 } // End if.
		 });
	 } // End of setChooseFileHandler method.

	 /**
	  * Loops through all of file_chooser's components and
	  * attempts to apply the Font, FCHOOSER_FONT to all
	  * components. Adapted from (stackoverflow.com/questions
	  * /45791492/ java-how-can-i-increase-the-font-of-the-
	  * folder-names-in-jfilechooser)
	  *
	  * <hr>
	  *
	  * @param comp The array consisting of all Component objects in file_chooser.
	  */
	 private void setFileChooserFont (Component[] comp)
	 {
		 for (int i = 0; i < comp.length; i++)
		 {
			 if (comp[i] instanceof Container)
			 {
				 setFileChooserFont(((Container) comp[i]).getComponents( ));
			 }
			 try
			 {
				 comp[i].setFont(FCHOOSER_FONT);
			 }
			 catch (Exception ex)
			 {
			 } // Meh.
		 }
	 }

 } // End class.
