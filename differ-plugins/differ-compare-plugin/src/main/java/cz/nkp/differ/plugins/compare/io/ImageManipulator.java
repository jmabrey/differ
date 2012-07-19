package cz.nkp.differ.plugins.compare.io;

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import cz.nkp.differ.plugins.ComparePluginInterface;

public class ImageManipulator {
	
	private static Logger LOGGER = ComparePluginInterface.LOGGER;
	
	public static class ImageManipulationException extends Exception{

		public ImageManipulationException(String string) {
			super(string);
			LOGGER.warn(string);
		}
	}	
	
	public static BufferedImage XORImages(BufferedImage image1, BufferedImage image2) throws ImageManipulationException{
		if(image1 == null || image2 == null){
			throw new ImageManipulationException("Cannot XOR images that are null. XOR'ing failed.");
		}
		
		if(image1.getWidth(null) != image2.getWidth(null) ||
		   image1.getHeight(null) != image2.getHeight(null)){
			throw new ImageManipulationException("Cannot XOR images that are differing dimensions. XOR'ing failed.");
		}
		
		if(image1.getTransparency() != image2.getTransparency()){
			throw new ImageManipulationException("Cannot XOR images that are differing transparencies. XOR'ing failed.");
		}
		
		if(image1.getType() != image2.getType()){
			throw new ImageManipulationException("Cannot XOR images that are differing data layout types. XOR'ing failed.");
		}
		
		int image_width = image1.getWidth(null);
		int image_height = image1.getHeight(null);
		int image_total_pixels = image_width * image_height;
		
		int[] combo1Pixels = new int[image_total_pixels];
		int[] combo2Pixels = new int[image_total_pixels];
		int[] imagePixels = new int[image_total_pixels];
		
		image1.getRGB(0, 0, image_width, image_height, combo1Pixels, 0, image_width); //Get all pixels
		image2.getRGB(0, 0, image_width, image_height, combo2Pixels, 0, image_width); //Get all pixels
		
		for(int pixel = 0; pixel < image_total_pixels; pixel++){
			imagePixels[pixel] = combo1Pixels[pixel] ^ combo2Pixels[pixel];
		}

		BufferedImage imageXOR = new BufferedImage(image_width, image_height, image1.getType());
		imageXOR.setRGB(0, 0, image_width, image_height, imagePixels, 0, image_width); //Set all pixels
		
		return imageXOR;
	}
	
	public static Image getBitmapScaledImage(BufferedImage image, int width, boolean scaleFit) throws ImageManipulationException{
		if(image == null || width < 1){
			throw new ImageManipulationException("Inavlid image or image scale width. Image scaling failed");
		}
		
		int height;
		
		if(scaleFit){
			double scale = ((double)image.getWidth())/( (double)width);
			height = (int) (image.getHeight()*scale);
			
			if(height > width * 2){
				height = width * 2;
			}
		}
		else{
			height = image.getHeight();
		}
		
		return image.getScaledInstance(width, height, BufferedImage.SCALE_FAST);
	}
}
