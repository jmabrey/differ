package cz.nkp.differ.plugins.compare.io;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ImageHistogramDatasetProcessor{	
	public static final XYDataset getXYDatasets(BufferedImage image){
		
		int image_width = image.getWidth();
		int image_height = image.getHeight();
		
		int image_total_pixels = image_width * image_height;
		
		XYSeries redChannel = new XYSeries("Red");
		XYSeries greenChannel = new XYSeries("Green");
		XYSeries blueChannel = new XYSeries("Blue");
		
		int[] imageRGBValues = new int[image_total_pixels];
		image.getRGB(0, 0, image_width, image_height, imageRGBValues, 0, image_width); //Get all pixels
		
		for(int x=0; x<image_width; x++) {
	        for(int y=0; y<image_height; y++) {
	        	int thisPixel = x * y;
	        	int rgbCombined= imageRGBValues[thisPixel];
	        	
	            int red = new Color(rgbCombined).getRed();
	            redChannel.add(thisPixel,red);
	            
	            int green = new Color(rgbCombined).getGreen();
	            greenChannel.add(thisPixel,green);
	            
	            int blue = new Color(rgbCombined).getBlue(); 
	            blueChannel.add(thisPixel,blue);
	        }
	    }
		
		XYSeriesCollection rgb = new XYSeriesCollection();
		rgb.addSeries(redChannel);
		rgb.addSeries(greenChannel);
		rgb.addSeries(blueChannel);
		
		return rgb;
	}
}
