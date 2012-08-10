package cz.nkp.differ.plugins.compare.io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.vaadin.addon.JFreeChartWrapper;

import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.plugins.BufferedImageStreamResource;
import cz.nkp.differ.plugins.DifferPluginInterface;
import cz.nkp.differ.plugins.ScrollableImagePanel;
import cz.nkp.differ.plugins.ScrollableImagePanel.ScrollableImagePanelException;
import cz.nkp.differ.plugins.compare.io.FileLoader.FileLoadingException;
import cz.nkp.differ.plugins.compare.io.ImageManipulator.ImageManipulationException;

public class ImageFileAnalysisContainer{
	
	/**
	* this gets rid of exception for not using native acceleration as well
	* as the djvu debug info on the output console
	*/
	static
	{
		System.setProperty("com.sun.media.jai.disableMediaLib", "true");
		com.lizardtech.djvu.DjVuOptions.out =
				com.lizardtech.djvu.DjVuOptions.err =
					new PrintStream(new OutputStream(){public void write(int c){}});

	}
	

	private static DifferPluginInterface parent;
			
	private static final int COMPONENT_SIZE_SCALE_FACTOR = 300;
	
	private boolean errorFlag = false;
	private String errorMessage = "Unknown Error";
	private String title = null;
	
	private ImageDatasetProcessor processor = null;
	
	private boolean synthesizedImage = false;
	private BufferedImage createdImage = null;
	
	private FileLoader fileHandle = null;
	
	public ImageFileAnalysisContainer(File f, DifferPluginInterface parent){
			ImageFileAnalysisContainer.parent = parent;
			
			
			fileHandle = new FileLoader(f);
			
			if(fileHandle.isValid() != FileLoader.FileValidation.VALID){
				setErrorState(new IOException("File is invalid."));
				return;
			}
			
			title = f.getName();
			
			try {
				processor = new ImageDatasetProcessor(fileHandle.getImage());
			} catch (FileLoadingException e) {
				setErrorState(e);
				return;
			}
	}
	
	public static ImageFileAnalysisContainer getCombinationContainer(ImageFileAnalysisContainer cont1,ImageFileAnalysisContainer cont2){
		ImageFileAnalysisContainer container = new ImageFileAnalysisContainer();
		
		if(cont1 == null || cont2 == null){
			throw new NullPointerException("ImageFileAnalysisContainers passed to combination constructor were null");
		}
		
		try {
			BufferedImage image1 = cont1.getImage();
			BufferedImage image2 = cont2.getImage();
			container = new ImageFileAnalysisContainer(image1,image2);
		} catch (FileLoadingException e) {
			container = new ImageFileAnalysisContainer();
			container.setErrorState(e);
			return container;
		}		
		
		return container;
	}
	
	private ImageFileAnalysisContainer(BufferedImage combo1, BufferedImage combo2){
		if(combo1 == null || combo2 == null){
			setErrorState(new IOException("Cannot create combination image because files are null."));
			return;
		}
		
		title = "Comparison";
		
		try {
			createdImage = ImageManipulator.XORImages(combo1, combo2);
			processor = new ImageDatasetProcessor(createdImage);
			synthesizedImage = true;
		} catch (ImageManipulationException e) {
			setErrorState(e);
			synthesizedImage = false;
			return;
		}
	}
	
	private ImageFileAnalysisContainer(){
		//Used only for blank error container that never even attempts to load anything
	}
	
	public BufferedImage getImage() throws FileLoadingException, NullPointerException{	
		if(!synthesizedImage || createdImage == null){
			return fileHandle.getImage();
		}
		else{
			return createdImage;
		}
	}
	
	public Component getMD5(){
		Label md5Label = new Label();
		String md5 = processor.getImageMD5();
		md5Label.setCaption("MD5: " + md5);
		return md5Label;
	}
	
	public Component getHistogram(){
	    JFreeChart histogram = ChartFactory.createXYLineChart(
	    		"",
	    		"",
	    		"",
	    		processor.getHistogramDataset(),
	    		PlotOrientation.VERTICAL,
	    		false,
	    		false,
	    		false
	    );
	    
	    histogram.setBackgroundPaint(Color.WHITE);
	    
	    // get a reference to the plot for further customization...
        XYPlot plot = histogram.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);  
        plot.setRangeGridlinesVisible(true);  
        plot.setRangeGridlinePaint(Color.GRAY);  
        plot.setDomainGridlinePaint(Color.GRAY); 

	    JFreeChartWrapper chartComponent = new JFreeChartWrapper(histogram,JFreeChartWrapper.RenderingMode.PNG);
	    
	    chartComponent.setGraphHeight(COMPONENT_SIZE_SCALE_FACTOR - 25);
	    chartComponent.setGraphWidth(COMPONENT_SIZE_SCALE_FACTOR - 25);
	    
	    chartComponent.setWidth(COMPONENT_SIZE_SCALE_FACTOR - 25, Component.UNITS_PIXELS);
	    chartComponent.setHeight(COMPONENT_SIZE_SCALE_FACTOR - 25, Component.UNITS_PIXELS);
	 
	    return chartComponent;
	}
			
	private Layout getErrorComponent(String message){
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setWidth(COMPONENT_SIZE_SCALE_FACTOR, Component.UNITS_PIXELS);
		layout.addComponent(new Label(message));
		return layout;
	}
	
	public Layout getComponent(){
		if(errorFlag){
			return getErrorComponent(errorMessage);
		}
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setWidth(COMPONENT_SIZE_SCALE_FACTOR, Component.UNITS_PIXELS);
		
		BufferedImage image;
		
		try {
			image = getImage();
			Embedded imageScaled = new Embedded(title,new BufferedImageStreamResource(
					ImageManipulator.getBitmapScaledImage(image, COMPONENT_SIZE_SCALE_FACTOR,true),parent));
			imageScaled.setType(Embedded.TYPE_IMAGE);
			
			int fullSizeWidth = (int) (((WebApplicationContext)parent.getApplication().getContext()).getBrowser().getScreenWidth() * .75);
			if(fullSizeWidth > image.getWidth()){
				fullSizeWidth = image.getWidth();
			}
			Embedded imageFull = new Embedded(title,new BufferedImageStreamResource(
					ImageManipulator.getBitmapScaledImage(image,fullSizeWidth,true),parent));
			imageFull.setType(Embedded.TYPE_IMAGE);
			
			layout.addComponent(new ScrollableImagePanel(imageScaled,imageFull,COMPONENT_SIZE_SCALE_FACTOR, parent));

		} catch (FileLoadingException e) {
			setErrorState(e);
			return getErrorComponent(errorMessage);
		} catch (IOException e) {
			setErrorState(e);
			return getErrorComponent(errorMessage);
		} catch (ImageManipulationException e) {
			setErrorState(e);
			return getErrorComponent(errorMessage);
		} catch (ScrollableImagePanelException e) {
			setErrorState(e);
			return getErrorComponent(errorMessage);
		}		
		
		layout.addComponent(getMD5());
		layout.addComponent(getHistogram());
		return layout;
	}
	
	private void setErrorState(Exception e){
		this.errorFlag = true;
		this.errorMessage = e.getLocalizedMessage();
		parent.getLogger().warn(e);
		e.printStackTrace();
	}
	
}





