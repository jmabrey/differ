package cz.nkp.differ.plugins.tools;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.vaadin.terminal.StreamResource;

import cz.nkp.differ.plugins.DifferPluginInterface;

public class BufferedImageStreamResource extends StreamResource{
	
	private static String FILE_EXT = "png";
	private static String MIME_TYPE = "image/png";

	
	public BufferedImageStreamResource(Image image,DifferPluginInterface parent) throws IOException {
		super(new BufferedImageStreamResource.BufferedImageStreamResourceSource(image,parent),"image-" + System.nanoTime()+"." + FILE_EXT ,parent.getApplication());
		setMIMEType(MIME_TYPE);
		setCacheTime(-1);
	}

	public static final class BufferedImageStreamResourceSource implements StreamResource.StreamSource{

		private Image image;
		private DifferPluginInterface parent;
		
		public BufferedImageStreamResourceSource(Image bi,DifferPluginInterface parent) throws IOException{
			if(bi == null){
				throw new IOException("Cannot create an image stream resource with a null image");
			}
			image = bi;
		}


		@Override
		public InputStream getStream() {
			ByteArrayOutputStream imagebuffer = null;
			try {
				/* Write the image to a buffer. */
				BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

				Graphics g = bimage.getGraphics();
				g.drawImage(image, 0, 0,null);
				g.dispose();
				
				ImageIO.setUseCache(false);
				imagebuffer = new ByteArrayOutputStream();
				ImageIO.write(bimage, FILE_EXT, imagebuffer);
				bimage = null;
				imagebuffer.flush();
				/* Return a stream from the buffer. */
				return new ByteArrayInputStream(imagebuffer.toByteArray());			
			} catch (IOException e) {
				parent.getLogger().error("Unable to write image to stream!",e);
				return null;
			} finally{
				if(imagebuffer != null){
					try {
						imagebuffer.close();
					} catch (IOException e) {
						parent.getLogger().error("Unable to close image buffer stream!",e);
					}
				}
			}
		}
	}
}

