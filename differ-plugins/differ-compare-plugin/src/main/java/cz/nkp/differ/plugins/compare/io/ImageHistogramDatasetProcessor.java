package cz.nkp.differ.plugins.compare.io;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ImageHistogramDatasetProcessor{	
	public static final XYDataset getXYDatasets(BufferedImage image){
		
		XYSeries redChannel = new XYSeries("Red");
		XYSeries greenChannel = new XYSeries("Green");
		XYSeries blueChannel = new XYSeries("Blue");
		
		int image_width = image.getWidth();
		int image_height = image.getHeight();
		int image_total_pixels = image_width * image_height;
	 
		int[] imageRGBValues = new int[image_total_pixels];
		int[][] bins = new int[3][256];
		image.getRGB(0, 0, image_width, image_height, imageRGBValues, 0, image_width); //Get all pixels
		
		for ( int thisPixel = 0; thisPixel < image_total_pixels; thisPixel++ ) {
			
        	int rgbCombined= imageRGBValues[thisPixel];
        	
            int red = new Color(rgbCombined).getRed();
            bins[0][red]++;
            
            int green = new Color(rgbCombined).getGreen();
            bins[1][green]++;
            
            int blue = new Color(rgbCombined).getBlue(); 
            bins[2][blue]++;
	    }
		
		for(int i = 0; i < 256; i++){
			redChannel.add(i,bins[0][i]);
			greenChannel.add(i,bins[1][i]);
			blueChannel.add(i,bins[2][i]);
		}
		
		XYSeriesCollection rgb = new XYSeriesCollection();
		rgb.addSeries(redChannel);
		rgb.addSeries(greenChannel);
		rgb.addSeries(blueChannel);
		
		return rgb;
	}
}
