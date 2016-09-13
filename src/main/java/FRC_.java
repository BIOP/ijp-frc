import ch.epfl.biop.frc.FRC.ThresholdMethod;
import ch.epfl.biop.frc.FRC;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;



public class FRC_ implements PlugIn {
	
	private static ResultsTable rt;
	
	@Override
	public void run(String arg) {
		// Find number of current images
		int n_images = WindowManager.getImageCount();
		
		String[] image_list = WindowManager.getImageTitles();
		
		// Prefill smartness
		if(n_images<2) {
			IJ.error("Need at least 2 images open for FRC Computation");
		}
		
		// FRC Thresholds
		ThresholdMethod[] thr_methods = ThresholdMethod.values();
		
		String[] thr_names = new String[thr_methods.length];
		int k=0;
		for(ThresholdMethod tm : thr_methods) {
			thr_names[k] = tm.toString();
			k++;
		}

		GenericDialog gd = new GenericDialog("FRC Calculation");
		gd.addChoice("Image_1", image_list, image_list[0]);
		gd.addChoice("Image_2", image_list, image_list[1]);
		gd.addChoice("Resolution Criteria",thr_names, thr_names[0]);
		
		gd.showDialog();
		
		if(gd.wasCanceled()) {
			return;
		}
		
		// Everything is ready now
		
		ImagePlus im1 = WindowManager.getImage(gd.getNextChoice());
		ImagePlus im2 = WindowManager.getImage(gd.getNextChoice());
		
		ThresholdMethod tm = thr_methods[gd.getNextChoiceIndex()];
		
		FRC frc = new FRC();
		
		double fire = frc.calculateFireNumber(im1.getProcessor(), im2.getProcessor(), tm);
		
		// Show results
		if (rt == null) {
			rt = new ResultsTable();

		}
		
		rt.incrementCounter();
		rt.addLabel(im1.getTitle()+":"+im2.getTitle());
		rt.addValue("FRC ["+tm+"]", fire);
		rt.addValue("FRC ["+tm+"] Calibrated ["+im1.getCalibration().getUnit()+"]", fire*im1.getCalibration().pixelHeight);
		rt.show("FRC Results");

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
		IJ.runPlugIn(clazz.getName(), "");

	}

}
