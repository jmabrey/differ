package cz.nkp.differ.plugins.compare.io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import cz.nkp.differ.plugins.ComparePluginInterface;

public class ImageDatasetProcessor{	
	
	private static Logger LOGGER = ComparePluginInterface.LOGGER;
	
	private static BufferedImage last_image = null;
	private static int[] imagePixelCache = null;
	private static int image_width_cached = 0,image_height_cached = 0;
	
	private static final void validateImageCache(BufferedImage image){
		if(last_image == null || !last_image.equals(image)){
			loadImageToCache(image);
		}
	}
	
	private static final void loadImageToCache(BufferedImage image){
		int image_width_cached = image.getWidth();
		int image_height_cached = image.getHeight();
		imagePixelCache = new int[image_width_cached * image_height_cached];
		image.getRGB(0, 0, image_width_cached, image_height_cached, imagePixelCache, 0, image_width_cached); //Get all pixels
		last_image = image;
	}
	
	public static final XYDataset getHistogramDataset(BufferedImage image){
		validateImageCache(image);		
		
		XYSeries redChannel = new XYSeries("Red");
		XYSeries greenChannel = new XYSeries("Green");
		XYSeries blueChannel = new XYSeries("Blue");
		
		int[][] bins = new int[3][256];
		
		for ( int thisPixel = 0; thisPixel < image_width_cached * image_height_cached; thisPixel++ ) {
			
        	int rgbCombined= imagePixelCache[thisPixel];
        	
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
	
	public static final String getImageMD5(BufferedImage image){
		validateImageCache(image);
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		
		for(int i : imagePixelCache){
			byteStream.write(i);
		}
		String md5 = DigestUtils.md5Hex(byteStream.toByteArray());
		try {
			byteStream.close();
		} catch (IOException e) {
			LOGGER.warn("Unable to close outputstream while generating md5");
		}
		
		return md5;
	}
}
