package cz.nkp.differ.plugins.compare.io;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.vaadin.terminal.StreamResource;

import cz.nkp.differ.plugins.ComparePluginInterface;

class BufferedImageStreamResource extends StreamResource{

	public BufferedImageStreamResource(Image image) throws IOException {
		super(new BufferedImageStreamResource.BufferedImageStreamResourceSource(image),"image",ComparePluginInterface.getApplication());
	}

	private static Logger LOGGER = ComparePluginInterface.LOGGER;

	public static final class BufferedImageStreamResourceSource implements StreamResource.StreamSource{

		public BufferedImageStreamResourceSource(Image bi) throws IOException{
			LOGGER = ComparePluginInterface.LOGGER;
			if(bi == null){
				throw new IOException("Cannot create an image stream resource with a null image");
			}
			image = bi;
			
			if(getStream() == null){
				throw new IOException("Stream is null, resource is invalid.");//Needed to make sure errors are always turned into UI events
			}
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
}

