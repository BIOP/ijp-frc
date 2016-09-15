import java.io.File;

import ch.epfl.biop.frc.FRC;
import ch.epfl.biop.frc.FRC.ThresholdMethod;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;


/**
 * @author Olivier Burri
 * BioImaging And Optics Platform (BIOP)
 * EPFL Lausanne, Switzerland
 * 
 * The FRC_ Plugin exposes the functionality of the FRC class originally written
 * by Alex Herbert at The Genome Damage and Stability Centre, University of Sussex
 * 
 * Copyright (C) 2016  Olivier Burri
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class FRC_ implements PlugIn {
	
	private static ResultsTable rt;
	private static final String pref = "biop.frc.";
	@Override
	public void run(String arg) {
		
		// FRC Thresholds
		ThresholdMethod[] thr_methods = ThresholdMethod.values();
				
		String[] thr_names = new String[thr_methods.length];
		int k=0;
		for(ThresholdMethod tm : thr_methods) {
			thr_names[k] = tm.toString();
			k++;
		}
		
		// Initialize Results Table
		if (rt == null || WindowManager.getWindow("FRC Results") == null)
			rt = new ResultsTable();

		// Call FRC Class
		FRC frc = new FRC();

		
		// Default Threshold Method
		int thr_i 	 = (int) Prefs.get(pref+"thrm.index", 0);
		
		// Default plot choice
		boolean is_plot = Prefs.get(pref+"is.plot", false);

		if(arg.compareTo("batch") == 0) {

			// Grab previous directories
			String dir1  = Prefs.get(pref+"dir1", "");
			String dir2  = Prefs.get(pref+"dir2", "");
						
			// Different Dialog
			GenericDialogPlus gdp = new GenericDialogPlus("Batch FRC Calculation");
			
			gdp.addMessage("Two folders must be provided.\nEach folder has a matching file in the other.");
			gdp.addDirectoryField("First Folder", dir1);
			gdp.addDirectoryField("Second Folder", dir2);
			gdp.addChoice("Resolution Criteria",thr_names, thr_names[thr_i]);
			gdp.addCheckbox("Save Plot", is_plot);

			gdp.showDialog();
			
			if(gdp.wasCanceled()) return;
			
			// Get choices
			dir1    = gdp.getNextString();
			dir2    = gdp.getNextString();
			thr_i   = gdp.getNextChoiceIndex();
			is_plot = gdp.getNextBoolean();
			
			// Save preferences
			Prefs.set(pref+"dir1", dir1);
			Prefs.set(pref+"dir2", dir2);
			Prefs.set(pref+"thrm.index", thr_i);
			Prefs.set(pref+"is.plot", is_plot);
						
			
			// Call the bath FIRE calculator method
			
			File d1 = new File(dir1);
			File d2 = new File(dir2);
			ThresholdMethod tm = thr_methods[thr_i];
						
			frc.batchCalculateFireNumber(d1, d2, tm, rt, is_plot);					
			
		} else {
			
			// Run plugin that will work on two open images
			
			// Get Current Images		
			String[] image_list = WindowManager.getImageTitles();
			
			if(image_list.length<2) {
				IJ.error("Need at least 2 images open for FRC Computation");
			}
	
			// Make Dialog choices
			GenericDialog gd = new GenericDialog("FRC Calculation");
			gd.addChoice("Image_1", image_list, image_list[0]);
			gd.addChoice("Image_2", image_list, image_list[1]);
			gd.addChoice("Resolution Criteria",thr_names, thr_names[thr_i]);
			gd.addCheckbox("Display Plot", is_plot);
			gd.showDialog();
			
			if(gd.wasCanceled()) {
				return;
			}
			
			// Everything is ready now
			ImagePlus im1 = WindowManager.getImage(gd.getNextChoice());
			ImagePlus im2 = WindowManager.getImage(gd.getNextChoice());
			thr_i = gd.getNextChoiceIndex();
			is_plot = gd.getNextBoolean();

			ThresholdMethod tm = thr_methods[thr_i];
			
			// Set Preferences again
			Prefs.set(pref+"thrm.index", thr_i);
			Prefs.set(pref+"is.plot", is_plot);

			
			// Finally calculate FRC
			double[][] frc_curve = frc.calculateFrcCurve(im1.getProcessor(), im2.getProcessor());
			
			// Smooth Curve using LOESS
			double[][] smooth_frc = frc.getSmoothedCurve(frc_curve);
			
			// Fourier Image REsolution number ("FIRE")
			double fire = frc.calculateFireNumber(smooth_frc, tm);
			
			// Append results
			rt.incrementCounter();
			rt.addLabel(im1.getTitle()+":"+im2.getTitle());
			rt.addValue("FRC ["+tm+"]", fire);
			rt.addValue("FRC ["+tm+"] Calibrated ["+im1.getCalibration().getUnit()+"]", fire*im1.getCalibration().pixelHeight);
			rt.show("FRC Results");
			
			
			// Plot if requested
			if(is_plot) {
				Plot p = frc.doPlot(frc_curve, smooth_frc, tm, fire, im1.getTitle());
				p.show();
			}			
		}
	}

	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = FRC_.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();
		// open the blobs sample
		ImagePlus image1 = IJ.openImage("http://imagej.net/images/blobs.gif");
		ImagePlus image2 = image1.duplicate();
		IJ.run(image2, "Add Noise", "");
		IJ.run(image1, "Add Noise", "");
		
		
		image1.show();
		image2.show();
		
		
		// run the plugin
		IJ.runPlugIn(clazz.getName(),"");
		//IJ.runPlugIn(clazz.getName(),"batch"); // Running the plugin to test batch processing.
		
	}

}
