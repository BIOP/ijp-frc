Fourier Ring Correlation ImageJ Plugin
======================================

Making use of the Fourier Ring Correlation Implementation by Alex Herbert
which is itself 'adapted from the FIRE (Fourier Image REsolution) plugin produced as part of the paper
Niewenhuizen, et al (2013). Measuring image resolution in optical nanoscopy. Nature Methods, 10, 557

http://www.nature.com/nmeth/journal/v10/n6/full/nmeth.2448.html

It is implemented as an Eclipse Plugin, with Maven dependencies for ImageJ/Fiji.

Maintained by Olivier Burri at the BioImaging and Optics Platform (BIOP)
http://biop.epfl.ch

## Installation

Please refer to the instructions on the Wiki page of the plugin, at http://imagej.net/Fourier_Ring_Correlation_Plugin#Installation

## Source Code

Our institution is testing a platform called C4Science, where this file is hosted. Simply use the _clone url_ that is provided above.
The simplest way is to use e2git for Eclipse and use "Check out Maven project from SCM" from Eclipse's *Import* menu

## Todo

Feel free to let us know if more functionality should be added. Perhaps implement the Fourier Shell Correlation for 3D data.

## Using the Plugin

You need two images open, the plugin will prompt for the Threshold method to use and will provide the FIRE number as a Results Table and display the FRC curve in a new Plot window. 

## Batch Mode

You need two folders where each folder has files with the same filenames. The plugin will open each pair of files and run the FRC Calculation on them. It can save the FRC Curve as an image. 