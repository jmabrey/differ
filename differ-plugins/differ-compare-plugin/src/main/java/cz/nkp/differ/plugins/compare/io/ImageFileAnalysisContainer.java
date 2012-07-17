package cz.nkp.differ.plugins.compare.io;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.vaadin.addon.JFreeChartWrapper;

import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvu.Document;
import com.lizardtech.djvubean.DjVuImage;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.plugins.ComparePluginInterface;

public class ImageFileAnalysisContainer{
	
	private Logger LOGGER;
	/**
	* this gets rid of exception for not using native acceleration
	*/
	static
	{
		System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	}
	
	private static enum FileType{
		JPEG,JPEG2000,TIFF,DJVU,PNG,COMBONATION,OTHER
	};
	
	private static enum FileValidation{
		VALID,
		FILE_TOO_LARGE,
		WRONG_FILE_TYPE,
		NULL_FILE_HANDLE,
		FILE_UNREADABLE
	}
		
	private static final long VALID_FILE_SIZE = 15 * FileUtils.ONE_MB;
	
	private FileType type = null;
	private File file = null;
	private String title;
	private BufferedImage image = null;
	
	public ImageFileAnalysisContainer(File f) throws IOException{
		file = f;
		LOGGER = ComparePluginInterface.LOGGER;
		FileValidation validFileResult = isValid();
		if(validFileResult != FileValidation.VALID){
			throw new IOException("The file is not valid: FileValidation." + validFileResult.toString());
		}
		title = f.getName();
	}
	
	private ImageFileAnalysisContainer(BufferedImage combo1, BufferedImage combo2) throws ImageReadException, IOException{
		type = FileType.COMBONATION;
		LOGGER = ComparePluginInterface.LOGGER;		
		getImage(combo1,combo2);
	}
	
	public FileValidation isValid(){
		
		if(file == null){
			return FileValidation.NULL_FILE_HANDLE;
		}
		
		if(file.isDirectory() || !file.canRead()){
			return FileValidation.FILE_UNREADABLE;
		}
			
		FileType type = getFileType();
		if(type == FileType.OTHER){
			return FileValidation.WRONG_FILE_TYPE;		
		}
		
		long fileSize = FileUtils.sizeOf(file);
		
		if(fileSize > VALID_FILE_SIZE){
			return FileValidation.FILE_TOO_LARGE;
		}
		
		return FileValidation.VALID;
	}
	
	public FileType getFileType(){
		if(type != null){
			return type;
		}

		String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
		if(extension.equals("jpeg") || extension.equals("jpg")){
			type = FileType.JPEG;
		}
		else if(extension.equals("jp2") || extension.equals("jpx") || extension.equals("jpf")){
			type = FileType.JPEG2000;
		}
		else if(extension.equals("tiff") || extension.equals("tif")){
			type = FileType.TIFF;
		}
		else if(extension.equals("djvu") || extension.equals("djv")){
			type = FileType.DJVU;
		}
		else if(extension.equals("png")){
			type = FileType.PNG;
		}
		else{
			type = FileType.OTHER;
		}
		
		return type;
	}
	
	public Dimension getDimension() throws ImageReadException, IOException{
		BufferedImage image_local = getImage();
		if(image_local == null){
			return null;
		}
		else return new Dimension(image_local.getWidth(),image_local.getHeight());
		
	}
	
	public BufferedImage getImage() throws ImageReadException, IOException{
		if(image != null){
			return image;
		}	
		return getImage(null,null);
	}
	
	private synchronized BufferedImage getImage(BufferedImage combo1, BufferedImage combo2) throws ImageReadException, IOException{
		switch(getFileType()){
			case JPEG:
				image = ImageIO.read(file);
				break;
			case JPEG2000:
				image = ImageIO.read(file);
				break;
			case TIFF:
				image = Imaging.getBufferedImage(file);
				break;
			case DJVU:
				 final Document document = new Document();
				 document.setAsync(false);
				 document.read(file.toURI().toURL());
				 final DjVuPage [] page = { document.getPage(0, DjVuPage.MAX_PRIORITY, true) };
				 final DjVuImage djvuImage = new DjVuImage(page,false);
				 Image image_local = djvuImage.getImage(new Frame(), djvuImage.getPageBounds(0))[0];
				 image = new BufferedImage(image_local.getWidth(null),image_local.getHeight(null),BufferedImage.TYPE_INT_ARGB);
				 Graphics g = image.createGraphics();
				 g.drawImage(image, 0, 0, null);
				 g.dispose();
				 break;
			case PNG:
				image = Imaging.getBufferedImage(file);
				break;		
			case COMBONATION:
				if(combo1 == null || combo2 == null){
					LOGGER.warn("Image was null.");
					image = null;
				}
				if(combo1.getWidth(null) != combo2.getWidth(null) ||
				   combo1.getHeight(null) != combo2.getHeight(null)){
					LOGGER.warn("Image dimensions are not the same.");
					image = null;
					break;
				}
				
				if(combo1.getType() != combo2.getType()){
					LOGGER.warn("Image types are not the same.");
					image = null;
					break;
				}
				
				int image_width = combo1.getWidth(null);
				int image_height = combo1.getHeight(null);

				image = new BufferedImage(image_width, image_height, combo1.getType());
				
				for(int x = 0;x < image_width; x++){
					for(int y = 0;y < image_height; y++){
						int combo1RGB = combo1.getRGB(x, y);
						int combo2RGB = combo2.getRGB(x, y);	
						image.setRGB(x,y,combo1RGB ^ combo2RGB);
					}
				}
				
				break;
			case OTHER:
				image = null;
				break;
			default:
				LOGGER.warn("Need to update switch statement",new RuntimeException());
				break;
		}
		
		if(image == null){
			throw new IOException("Unable to load the image.");
		}
		
		return image;
	}
	
	public static ImageFileAnalysisContainer getCombinationImageFileAnalysis(ImageFileAnalysisContainer cf1, ImageFileAnalysisContainer cf2) throws IOException, ImageReadException{
		if(cf1.isValid() == FileValidation.VALID && cf2.isValid() == FileValidation.VALID){
			return new ImageFileAnalysisContainer(cf1.getImage(),cf2.getImage());
		}
		else throw new IOException("Multi File Comparison failed, file invalid.");
	}
	
	public Component getMD5() throws ImageReadException, IOException{
		Label md5Label = new Label();
		String md5 = ImageDatasetProcessor.getImageMD5(getImage());
		md5Label.setCaption("MD5: " + md5);
		return md5Label;
	}
	
	public Component getHistogram() throws ImageReadException, IOException{
	    JFreeChart histogram = ChartFactory.createXYLineChart(
	    		"Histogram",
	    		"pixel",
	    		"value",
	    		ImageDatasetProcessor.getHistogramDataset(getImage()),
	    		PlotOrientation.VERTICAL,
	    		false,
	    		false,
	    		false
	    );
	    
	    histogram.setBackgroundPaint(Color.WHITE);
	    
	    // get a reference to the plot for further customization...
        Plot plot = histogram.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        
	    JFreeChartWrapper chartComponent = new JFreeChartWrapper(histogram,JFreeChartWrapper.RenderingMode.PNG);
	    
	    chartComponent.setGraphHeight(300);
	    chartComponent.setGraphWidth(300);
	    
	    chartComponent.setWidth(300, Component.UNITS_PIXELS);
	    chartComponent.setHeight(300, Component.UNITS_PIXELS);
	 
	    return chartComponent;
	}
	
	public Image getBitmapScaledImage(int width, int height) throws ImageReadException, IOException{
		BufferedImage image_local = getImage();
		if(image_local == null){
			return null;
		}
		return image_local.getScaledInstance(width, height, BufferedImage.SCALE_FAST);
	}
	
	public Layout getComponent() throws ImageReadException, IOException{
		VerticalLayout layout = new VerticalLayout();
		Embedded image = new Embedded(title,new BufferedImageStreamResource(getBitmapScaledImage(300,300)));
		image.setType(Embedded.TYPE_IMAGE);
		layout.addComponent(image);
		layout.addComponent(getMD5());
		layout.addComponent(getHistogram());
		return layout;
	}
	
}

class BufferedImageStreamResource extends StreamResource{

	public BufferedImageStreamResource(Image image) throws IOException {
		super(new BufferedImageStreamResourceSource(image),"image",ComparePluginInterface.getApplication());
	}
	
}

class BufferedImageStreamResourceSource implements StreamResource.StreamSource{

	public BufferedImageStreamResourceSource(Image bi) throws IOException{
		if(bi == null){
			throw new IOException("Cannot create an image stream resource with a null image");
		}
		image = bi;
	}
	
	private Image image;
	
	@Override
	public InputStream getStream() {
		try {
            /* Write the image to a buffer. */
			ByteArrayOutputStream imagebuffer = new ByteArrayOutputStream();
			
			BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics g = bimage.getGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			
            ImageIO.write(bimage, "png", imagebuffer);
            
            /* Return a stream from the buffer. */
            return new ByteArrayInputStream(imagebuffer.toByteArray());
        } catch (IOException e) {
        	ComparePluginInterface.LOGGER.error("Unable to write image to stream!");
            return null;
        }
	}
	
}