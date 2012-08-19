package cz.nkp.differ.plugins.tools;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.vaadin.Application;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;

public class Java2DImageComponentGenerator {

	private Image i;
	private Application app;
	private String FILE_EXT = "png";
	
	public Java2DImageComponentGenerator(Image i,Application app){
		if(i == null){
			throw new NullPointerException("Null Image");
		}
		
		this.i = i;
		this.app = app;
	}
	
	public Component getImageComponent(){
		try {
			File temp = File.createTempFile("image", "." + FILE_EXT);
			OutputStream stream = new BufferedOutputStream(new FileOutputStream(temp));
			
			/* Write the image to a buffer. */
			BufferedImage bimage = new BufferedImage(i.getWidth(null), i.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics g = bimage.getGraphics();
			g.drawImage(i, 0, 0,null);
			g.dispose();
			
			ImageIO.setUseCache(false);
			ImageIO.write(bimage, FILE_EXT, stream);
			bimage = null;
			stream.flush();
			stream.close();
			
			FileResource imageResource = new FileResource(temp,app);
			
			Embedded image = new Embedded(null,imageResource);
			image.setType(Embedded.TYPE_IMAGE);
			
			return image;
		} catch (IOException e) {
			return null;
		}
	}
	
}
