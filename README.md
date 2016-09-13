Fourier Ring Correlation Implementation
===========================================

Making use of the Fourier Ring Correlation Implementation by Alex Herbert
which is itself 'adapted from the FIRE (Fourier Image REsolution) plugin produced as part of the paper
Niewenhuizen, et al (2013). Measuring image resolution in optical nanoscopy. Nature Methods, 10, 557

http://www.nature.com/nmeth/journal/v10/n6/full/nmeth.2448.html

It is implemented as an Eclipse Plugin, with Maven dependencies for ImageJ/Fiji.

## TODO

* Implement a plot to display the FSC, the smoothed plot and the selected intercept to have a visual confirmation. 

## Batch Mode

You need two folders ('A' and 'B') where each folder has the same number of files with the same filenames. The plugin will open each pair of files and run the FRC on them.