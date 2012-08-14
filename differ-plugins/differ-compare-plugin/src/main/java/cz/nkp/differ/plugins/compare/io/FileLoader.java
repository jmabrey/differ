package cz.nkp.differ.plugins.compare.io;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvu.Document;
import com.lizardtech.djvubean.DjVuImage;

public class FileLoader {
	
	public static enum FileValidation{
		VALID,
		FILE_TOO_LARGE,
		WRONG_FILE_TYPE,
		NULL_FILE_HANDLE,
		FILE_UNREADABLE
	}
	
	public class FileLoadingException extends IOException{

		public FileLoadingException(String string) {
			super(string);
			LOGGER.warn(string);
		}
		
		public FileLoadingException(){
			super();
		}

		public FileLoadingException(Exception e) {
			super(e);
		}
	}		

	private static final long VALID_FILE_SIZE = 15 * FileUtils.ONE_MB;		
	
	private static enum FileType{
		JPEG,JPEG2000,TIFF,DJVU,PNG,OTHER
	};
	
	private static Logger LOGGER;	
	private FileType type = null;
	private File file = null;
	private BufferedImage image = null;
	
	public FileLoader(File load){
		file = load;
		//System.setProperty("com.sun.media.jai.disableMediaLib", "true");
		ImageIO.scanForPlugins();	
		for(String s: ImageIO.getReaderFormatNames()){
			System.out.println(s);
		}	
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
	
	public synchronized BufferedImage getImage() throws FileLoadingException{
		switch(getFileType()){
			case JPEG:
				loadJPEGImage();
				break;
			case JPEG2000:
				loadJPEG2000Image();
				break;
			case TIFF:
				loadTIFFImage();
				break;
			case DJVU:
				loadDJVUImage();
				break;
			case PNG:
				loadPNGImage();
				break;		
			case OTHER:
				
				break;
			default:
				throw new FileLoadingException("Need to update switch statement");
		}
		
		return image;
	}
	
	protected BufferedImage loadDJVUImage() throws FileLoadingException{
		 Document document = new Document();
		 document.setAsync(false);
		 Image image_local;
		 
		 try {
			document.read(file.toURI().toURL());
			DjVuPage[] page = { document.getPage(0, DjVuPage.MAX_PRIORITY, true) };
			DjVuImage djvuImage = new DjVuImage(page,true);
			image_local = djvuImage.getImage(new Canvas(), djvuImage.getPageBounds(0))[0];
		 } catch (MalformedURLException e) {
		 	throw new FileLoadingException(e);
		 } catch (IOException e) {
	 		throw new FileLoadingException(e);
	     }	 
		 
		 image = new BufferedImage(image_local.getWidth(null),image_local.getHeight(null),BufferedImage.TYPE_INT_ARGB);
		 
		 Graphics g = image.createGraphics();
		 g.drawImage(image_local, 0, 0, null);
		 g.dispose();
		 
		 return image;
	}
	
	protected BufferedImage loadJPEGImage() throws FileLoadingException{
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			throw new FileLoadingException(e);
		}
		
		return image;
	}
	
	protected BufferedImage loadJPEG2000Image() throws FileLoadingException{
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			throw new FileLoadingException(e);
		}
		
		return image;
	}
	
	protected BufferedImage loadTIFFImage() throws FileLoadingException{
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			throw new FileLoadingException(e);
		}
		
		return image;
	}
	
	protected BufferedImage loadPNGImage() throws FileLoadingException{
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			throw new FileLoadingException(e);
		}
		
		return image;
	}
}
