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
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import cz.nkp.differ.plugins.DifferPluginInterface;
import cz.nkp.differ.plugins.compare.io.FileLoader.FileLoadingException;
import cz.nkp.differ.plugins.compare.io.ImageManipulator.ImageManipulationException;
import cz.nkp.differ.plugins.compare.io.generators.HashComponentGenerator;
import cz.nkp.differ.plugins.compare.io.generators.HistogramComponentGenerator;
import cz.nkp.differ.plugins.tools.DelayedComponentGenerator;
import cz.nkp.differ.plugins.tools.Java2DImageComponentGenerator;
import cz.nkp.differ.plugins.tools.ScrollableImagePanel;
import cz.nkp.differ.plugins.tools.ScrollableImagePanel.ScrollableImagePanelException;

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
			
	private static final int COMPONENT_SIZE_SCALE_FACTOR = 400;
	
	private boolean errorFlag = false;
	private String errorMessage = "Unknown Error";
	
	public ImageDatasetProcessor imageProcessor = null;
	public ImageMetadataProcessor metadataProcessor = null;
	
	private boolean isSynthesizedImage = false;
	private BufferedImage synthesizedImage = null;
	private boolean hashesEqual = false;
	
	private FileLoader fileHandle = null;
	
	private String title = "Image";
	
	public ImageFileAnalysisContainer(File f, DifferPluginInterface parent){
			ImageFileAnalysisContainer.parent = parent;			
			
			title = f.getName();
			
			fileHandle = new FileLoader(f);
			
			if(fileHandle.isValid() != FileLoader.FileValidation.VALID){
				setErrorState(new IOException("File is invalid."));
				return;
			}
			
			try {
				imageProcessor = new ImageDatasetProcessor(fileHandle.getImage());
			} catch (FileLoadingException e) {
				setErrorState(e);
				return;
			}
			
			metadataProcessor = new ImageMetadataProcessor(f, fileHandle.getFileType());			
	}
	
	public ImageFileAnalysisContainer(ImageFileAnalysisContainer iFAC1, ImageFileAnalysisContainer iFAC2, DifferPluginInterface parent) throws FileLoadingException, NullPointerException{
		this(iFAC1.getImage(),iFAC2.getImage(),iFAC1.imageProcessor.getImageMD5(),iFAC2.imageProcessor.getImageMD5(),parent);
	}
	
	public ImageFileAnalysisContainer(BufferedImage image1, BufferedImage image2,String hash1, String hash2, DifferPluginInterface parent){
		ImageFileAnalysisContainer.parent = parent;
		isSynthesizedImage = true;
		
		title = "Compare";
		
		try {
			synthesizedImage = ImageManipulator.XORImages(image1, image2);
		} catch (ImageManipulationException e) {
			setErrorState(e);
			return;
		}
		
		if(hash1.equals(hash2)){
			hashesEqual = true;
		}
		
		imageProcessor = new ImageDatasetProcessor(synthesizedImage);
	}
	
	public BufferedImage getImage() throws FileLoadingException, NullPointerException{	
		if(!isSynthesizedImage || synthesizedImage == null){
			return fileHandle.getImage();
		}
		else{
			return synthesizedImage;
		}
	}
	
	public Component getHash(){
		Label hashLabel = new Label();
		HashComponentGenerator generator = new HashComponentGenerator(isSynthesizedImage,hashesEqual,imageProcessor);
		return DelayedComponentGenerator.handleComponent(hashLabel, generator);
	}
	
	public Component getHistogram(){
		Layout histogram = new VerticalLayout();
		HistogramComponentGenerator generator = new HistogramComponentGenerator(imageProcessor,COMPONENT_SIZE_SCALE_FACTOR);
		return DelayedComponentGenerator.handleComponent(histogram, generator);
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
		final VerticalLayout layout = new VerticalLayout();
		
		/*new Thread(){
			public void run(){
				 generateComponent(layout);
			}
		}.start();*/
		
		generateComponent(layout);
		
		return layout;
	}
	
	private void generateComponent(VerticalLayout layout){
		
		layout.addComponent(new Label(title));
		if(errorFlag){
			layout.addComponent(getErrorComponent(errorMessage));
			return;
		}
		
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setImmediate(true);
		layout.setWidth(COMPONENT_SIZE_SCALE_FACTOR, Component.UNITS_PIXELS);
		
		BufferedImage image;
		
		try {
			image = getImage();
			Component imageScaled = new Java2DImageComponentGenerator(
					ImageManipulator.getBitmapScaledImage(image, COMPONENT_SIZE_SCALE_FACTOR,true),
					parent.getApplication()).getImageComponent();
			
			int fullSizeWidth = (int) (((WebApplicationContext)parent.getApplication().getContext()).getBrowser().getScreenWidth() * .75);
			if(fullSizeWidth > image.getWidth()){
				fullSizeWidth = image.getWidth();
			}
			
			Component imageFull = new Java2DImageComponentGenerator(image,
					parent.getApplication()).getImageComponent();
			
			layout.addComponent(new ScrollableImagePanel(imageScaled,imageFull,COMPONENT_SIZE_SCALE_FACTOR, parent));

		} catch (FileLoadingException e) {
			setErrorState(e);
			layout.addComponent(getErrorComponent(errorMessage));
			return;
		} catch (ImageManipulationException e) {
			setErrorState(e);
			layout.addComponent(getErrorComponent(errorMessage));
			return;
		} catch (ScrollableImagePanelException e) {
			setErrorState(e);
			layout.addComponent(getErrorComponent(errorMessage));
			return;
		}		
		
		layout.addComponent(getHash());
		layout.addComponent(getHistogram());
		if(!isSynthesizedImage){
			layout.addComponent(metadataProcessor.getMetadata());
		}
	}
	
	private void setErrorState(Exception e){
		this.errorFlag = true;
		this.errorMessage = e.getLocalizedMessage();
		parent.getLogger().error(e);
	}
	
}







