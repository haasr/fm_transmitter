fmpirate
========

Use a simple GUI to toggle frequency, shuffle, and transmit WAV playlists over FM radio.

This project uses the general clock output to produce frequency modulated radio
communication. It is based on idea originaly posted here:
[http://icrobotics.co.uk/wiki/index.php/Turning_the_Raspberry_Pi_Into_an_FM_Transmitter]
(http://icrobotics.co.uk/wiki/index.php/Turning_the_Raspberry_Pi_Into_an_FM_Transmitter),
but does not use DMA controller in order to distribute samples to output (clock generator),
so sound quality is worse as in PiFm project and only mono transmition is available but this
makes possible to run it on all kind of boards. This project was forked from *markondej*
meaning I didn't do the hard part; I just made a nifty little Java GUI to make creating
and transmitting playlists easier.

Setup:
------

********************
1) Compile the code.
********************

To compile this project use commands below:

.. code:: bash

    sudo apt-get install make gcc g++ openjdk-8-jdk
    make
    ./compile-fmpirate


You will also need to install SoX to be able to use this project:

.. code:: bash

    sudo apt install sox



**********************************
2) Creating a valid playlist file.
**********************************

This program is only capable of transmitting WAV files. In order for the
program to transmit your WAV files, they must be placed in the root of
the "fmpirate" directory. In order to play a playlist, you must first
create one.

Each line in a playlist file should only consist of a file name followed
by a pipe ('|') delimeter and a specified amount of gain
(i.e. 'star_wars.wav|gain +5'). If the file name contains spaces, there is
no need to delimit them with backslashes. Below is a sample playlist file:

::
    # playlist.txt -- must use txt extension.
    Ride.wav|gain +5
    # you can insert comments like this.
    Lake Shore Drive.wav|gain +3
    Redbone.wav|gain +0
    Hello-Goodbye.wav|gain +2
    Good Vibrations.wav|gain +5
    Bad Moon Rising.wav|gain +5 # this comment will break things.

As you can see above, you must use a txt file format. While you may comments
lines by starting them with a pound sign, do not put comments on the same
line as a song; fmpirate will not play nicely with the last line in the
example text file. While it is recommended you save your playlist files
in "fmpirate/FMPirate/Playlists/" for quickest access, you may save them
wherever you like. Of course, all the WAV files listed in the playlist
MUST be in the root of the "fmpirate" directory.

Usage
-----

After you have compiled and created a playlist, its time to test it out.
You can launch the program by navigating to "fmpirate/" and typing:

.. code:: bash

    ./fmpirate


After the GUI launches you may select "Import" and navigate to your
playlist file of choice and select it. Then toggle the frequency to
that which you would like to broadcast over. Now you may play, pause,
and shuffle the playlist.


****
Law
****

Please keep in mind that transmitting on certain frequencies without
special permissions may be illegal in your country. Don't sue me.