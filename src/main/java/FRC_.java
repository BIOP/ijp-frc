import ch.epfl.biop.frc.FRC.ThresholdMethod;
import fiji.util.gui.GenericDialogPlus;

import java.io.File;

import ch.epfl.biop.frc.FRC;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.gui.Plot;


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
		if (rt == null)
			rt = new ResultsTable();

		// Call FRC Class
		FRC frc = new FRC();

		// Default Threshold
		int thr_i 	 = (int) Prefs.get(pref+"thrm.index", 0);
		
		// Default plot choice
		boolean is_plot = Prefs.get(pref+"is.plot", false);

		if(arg.compareTo("batch") == 0) {
			// Grab defaults
			String dir1  = Prefs.get(pref+"dir1", "");
			String dir2  = Prefs.get(pref+"dir2", "");
			
			
			
			// Run batch here
			GenericDialogPlus gdp = new GenericDialogPlus("Batch FRC Calculation");
			
			gdp.addMessage("Two folders must be provided.\nEach folder has a matching file in the other.");
			gdp.addDirectoryField("First Folder", dir1);
			gdp.addDirectoryField("Second Folder", dir2);
			gdp.addChoice("Resolution Criteria",thr_names, thr_names[thr_i]);
			gdp.addCheckbox("Save Plot", is_plot);

			gdp.showDialog();
			
			if(gdp.wasCanceled()) return;
			
			// Navigate the directories, and find files
			dir1    = gdp.getNextString();
			dir2    = gdp.getNextString();
			thr_i   = gdp.getNextChoiceIndex();
			is_plot = gdp.getNextBoolean();
			
			File d1 = new File(dir1);
			File d2 = new File(dir2);
			ThresholdMethod tm = thr_methods[thr_i];
			
			
			// Save preferences
			Prefs.set(pref+"dir1", dir1);
			Prefs.set(pref+"dir2", dir2);
			Prefs.set(pref+"thrm.index", thr_i);
			Prefs.set(pref+"is.plot", is_plot);
			
			
			frc.batchCalculateFireNumber(d1, d2, tm, rt, is_plot);					
			
		} else {
			// Run normal plugin
			
			// Find number of current images
			int n_images = WindowManager.getImageCount();
			
			String[] image_list = WindowManager.getImageTitles();
			
			// Prefill smartness
			if(n_images<2) {
				IJ.error("Need at least 2 images open for FRC Computation");
			}
	
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
			
			// Smooth Curve
			double[][] smooth_frc = frc.getSmoothedCurve(frc_curve);
			
			// Fourier Image REsolution number ("FIRE")
			double fire = frc.calculateFireNumber(smooth_frc, tm);
							
			
			// Plot if requested
			if(is_plot) {
				Plot p = frc.doPlot(frc_curve, smooth_frc, tm, fire, im1.getTitle());
				p.show();
			}
			rt.incrementCounter();
			rt.addLabel(im1.getTitle()+":"+im2.getTitle());
			rt.addValue("FRC ["+tm+"]", fire);
			rt.addValue("FRC ["+tm+"] Calibrated ["+im1.getCalibration().getUnit()+"]", fire*im1.getCalibration().pixelHeight);
			rt.show("FRC Results");
			
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
		//IJ.runPlugIn(clazz.getName(),"batch"); // Running the plugin for batch processing.
		
	}

}
