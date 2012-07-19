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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

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
	private static final int COMPONENT_SIZE_SCALE_FACTOR = 300;
	
	
	private boolean errorFlag = false;
	private String errorMessage = "";
	private FileType type = null;
	private File file = null;
	private String title;
	private BufferedImage image = null;
	private ImageDatasetProcessor processor;
	
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
		title = "Comparison";
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
		BufferedImage imageToReturn = getImage(null,null);
		
		if(imageToReturn == null){
			imageToReturn = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
		}
		else{
			processor = new ImageDatasetProcessor(imageToReturn);
		}
		
		return imageToReturn;
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
					break;
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
				int image_total_pixels = image_width * image_height;
				
				int[] combo1Pixels = new int[image_total_pixels];
				int[] combo2Pixels = new int[image_total_pixels];
				int[] imagePixels = new int[image_total_pixels];
				
				combo1.getRGB(0, 0, image_width, image_height, combo1Pixels, 0, image_width); //Get all pixels
				combo2.getRGB(0, 0, image_width, image_height, combo2Pixels, 0, image_width); //Get all pixels
				
				for(int pixel = 0; pixel < image_total_pixels; pixel++){
					imagePixels[pixel] = combo1Pixels[pixel] ^ combo2Pixels[pixel];
				}

				image = new BufferedImage(image_width, image_height, combo1.getType());
				image.setRGB(0, 0, image_width, image_height, imagePixels, 0, image_width); //Set all pixels
				
				processor = new ImageDatasetProcessor(image);
				
				break;
			case OTHER:
				image = null;
				break;
			default:
				LOGGER.warn("Need to update switch statement",new RuntimeException());
				break;
		}
		
		if(image == null){
			errorFlag = true;
			errorMessage = "Unable to load the image.";
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
		String md5 = processor.getImageMD5();
		md5Label.setCaption("MD5: " + md5);
		return md5Label;
	}
	
	public Component getHistogram() throws ImageReadException, IOException{
	    JFreeChart histogram = ChartFactory.createXYLineChart(
	    		"Histogram",
	    		"pixel",
	    		"value",
	    		processor.getHistogramDataset(),
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
	    
	    chartComponent.setGraphHeight(COMPONENT_SIZE_SCALE_FACTOR - 25);
	    chartComponent.setGraphWidth(COMPONENT_SIZE_SCALE_FACTOR - 25);
	    
	    chartComponent.setWidth(COMPONENT_SIZE_SCALE_FACTOR - 25, Component.UNITS_PIXELS);
	    chartComponent.setHeight(COMPONENT_SIZE_SCALE_FACTOR - 25, Component.UNITS_PIXELS);
	 
	    return chartComponent;
	}
	
	public Image getBitmapScaledImage(int width, boolean scaleFit) throws ImageReadException, IOException{
		BufferedImage image_local = getImage();
		if(image_local == null){
			return null;
		}
		int height;
		
		if(scaleFit){
			double scale = ((double)image_local.getWidth())/( (double)width);
			height = (int) (image_local.getHeight()*scale);
			
			if(height > width * 2){
				height = width * 2;
			}
		}
		else{
			height = image_local.getHeight();
		}
		
		return image_local.getScaledInstance(width, height, BufferedImage.SCALE_FAST);
	}
	
	private static Layout getScrollableImagePanel(Component scaledImage,final Component fullImage){
		final Panel scrollPanel = new Panel();
		scrollPanel.addStyleName(Runo.PANEL_LIGHT);
		scrollPanel.setWidth(COMPONENT_SIZE_SCALE_FACTOR, Component.UNITS_PIXELS);
		scrollPanel.setHeight(COMPONENT_SIZE_SCALE_FACTOR, Component.UNITS_PIXELS);
		scrollPanel.setScrollable(true);
		
		HorizontalLayout scrollButtons = new HorizontalLayout();
		scrollPanel.addComponent(scrollButtons);
		        
		Button scrollUp = new Button("▲");
		scrollUp.addStyleName(Runo.BUTTON_SMALL);
		scrollUp.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		        int scrollPos = scrollPanel.getScrollTop() - 250;
		        if (scrollPos < 0)
		            scrollPos = 0;
		        scrollPanel.setScrollTop(scrollPos);
		    }
		});
		
		        
		Button scrollDown = new Button("▼");
		scrollDown.addStyleName(Runo.BUTTON_SMALL);
		scrollDown.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		        int scrollPos = scrollPanel.getScrollTop();
		        if (scrollPos > scrollPanel.getHeight())
		            scrollPos = (int) scrollPanel.getHeight();
		        scrollPanel.setScrollTop(scrollPos + 250);
		    }
		});
		
		Button fullSizeButton = new Button("Larger");
		fullSizeButton.addListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		        ComparePluginInterface.getApplication().getMainWindow().addWindow(new FullSizeImageWindow(fullImage));
		    }
		});
		
		scrollUp.setImmediate(true);
		scrollDown.setImmediate(true);
		fullSizeButton.setImmediate(true);
		
		scrollButtons.addComponent(scrollUp);
		scrollButtons.addComponent(scrollDown);
		scrollButtons.addComponent(fullSizeButton);
		
		
		scrollPanel.addComponent(scaledImage);
		
		VerticalLayout component = new VerticalLayout();
		component.addComponent(scrollPanel);
		component.addComponent(scrollButtons);
		
		return component;
	}
		
	public Layout getErrorComponent(String message){
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setWidth(COMPONENT_SIZE_SCALE_FACTOR, Component.UNITS_PIXELS);
		layout.addComponent(new Label(message));
		return layout;
	}
	
	public Layout getComponent() throws ImageReadException, IOException{
		if(errorFlag){
			return getErrorComponent(errorMessage);
		}
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setWidth(COMPONENT_SIZE_SCALE_FACTOR, Component.UNITS_PIXELS);
		
		Embedded imageScaled = new Embedded(title,new BufferedImageStreamResource(getBitmapScaledImage(COMPONENT_SIZE_SCALE_FACTOR,true)));
		imageScaled.setType(Embedded.TYPE_IMAGE);
		
		Embedded imageFull = new Embedded(title,new BufferedImageStreamResource(getBitmapScaledImage(720,false)));
		imageFull.setType(Embedded.TYPE_IMAGE);
		
		layout.addComponent(getScrollableImagePanel(imageScaled,imageFull));
		layout.addComponent(getMD5());
		layout.addComponent(getHistogram());
		return layout;
	}
	
}

class FullSizeImageWindow extends Window{
	public FullSizeImageWindow(Component fullImage){
		setCaption("Image Display");
		setModal(true);
		setDraggable(false);
		setResizable(false); 
		center();
		setWidth("800px");
		setHeight("100%");
		
		Panel imagePanel = new Panel();
		imagePanel.addComponent(fullImage);
		imagePanel.setScrollable(true);
		imagePanel.addStyleName(Runo.PANEL_LIGHT);
		imagePanel.setSizeFull();
		
		addComponent(imagePanel);
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