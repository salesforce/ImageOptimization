package com.salesforce.perfeng.uiperf.imageoptimization.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.salesforce.perfeng.uiperf.ThirdPartyBinaryNotFoundException;

/**
 * Utility methods used to interact with images. This class is thread safe.
 * 
 * @author eperret (Eric Perret)
 * @since 186.internal
 */
public class ImageUtils {

	private final static Logger logger = LoggerFactory.getLogger(ImageUtils.class);
	/**
	 * Name of Image Magic's {@value #CONVERT_BINARY} binary application used to
	 * convert one image into another image by changing it's file type. This 
	 * application needs to be installed on the system this JAVA app is running 
	 * on.
	 */
	static final String CONVERT_BINARY = "convert";
	
	private static final boolean equals(final int[] data1, final int[] data2) {
        final int length = data1.length;
		if (length != data2.length) {
			logger.debug("File lengths are different.");
            return false;
        }
		for(int i = 0; i < length; i++) {
			if(data1[i] != data2[i]) {
		        
				//If the alpha is 0 for both that means that the pixels are 100%
				//transparent and the color does not matter. Return false if 
				//only 1 is 100% transparent.
				if((((data1[i] >> 24) & 0xff) == 0) && (((data2[i] >> 24) & 0xff) == 0)) {
					logger.debug("Both pixles at spot {} are different but 100% transparent.", Integer.valueOf(i));
				} else {
					logger.debug("The pixel {} is different.", Integer.valueOf(i));
					return false;
				}
			}
		}
		logger.debug("Both groups of pixels are the same.");
		return true;
	}
	
	private static final int[] getPixels(final BufferedImage img, final File file) {
		
		final int width = img.getWidth();
        final int height = img.getHeight();
        int[] pixelData = new int[width * height];
        
		final Image pixelImg; 
		if (img.getColorModel().getColorSpace() == ColorSpace.getInstance(ColorSpace.CS_sRGB)) {
            pixelImg = img;
        } else {
            pixelImg = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null).filter(img, null);
        }
        
        final PixelGrabber pg = new PixelGrabber(pixelImg, 0, 0, width, height, pixelData, 0, width);
        
        try {
			if(!pg.grabPixels()) {
			    throw new RuntimeException();
			}
		} catch (final InterruptedException ie) {
			throw new ImageFileOptimizationException(file.getPath(), ie);
		}
		
        return pixelData;
	}
	
	/**
	 * Gets the {@link BufferedImage} from the passed in {@link File}.
	 * 
	 * @param file The <code>File</code> to use.
	 * @return The resulting <code>BufferedImage</code>
	 */
	@SuppressWarnings("unused")
	final static BufferedImage getBufferedImage(final File file) {
		Image image;
			
		try (final FileInputStream inputStream = new FileInputStream(file)) {
			// ImageIO.read(file) is broken for some images so I went this 
			// route
			image = Toolkit.getDefaultToolkit().createImage(file.getCanonicalPath());
			
			//forces the image to be rendered
			new ImageIcon(image);
		} catch(final Exception e2) {
			throw new ImageFileOptimizationException(file.getPath(), e2);
		}
		
		final BufferedImage converted = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = converted.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
		return converted;
	}
	
	/**
	 * Compares file1 to file2 to see if they are the same based on a visual 
	 * pixel by pixel comparison. This has issues with marking images different
	 * when they are not. Works perfectly for all images.
	 * 
	 * @param file1 First file to compare
	 * @param file2 Second image to compare
	 * @return <code>true</code> if they are equal, otherwise 
	 *         <code>false</code>.
	 */
	private final static boolean visuallyCompareJava(final File file1, final File file2) {
		return equals(getPixels(getBufferedImage(file1), file1), getPixels(getBufferedImage(file2), file2));
	}

	/**
	 * Compares file1 to file2 to see if they are the same based on a visual 
	 * pixel by pixel comparison. This has issues with marking images different
	 * when they are not. Works perfectly for all images.
	 * 
	 * @param file1 Image 1 to compare
	 * @param file2 Image 2 to compare
	 * @return <code>true</code> if both images are visually the same.
	 */
	public final static boolean visuallyCompare(final File file1, final File file2) {
		
		logger.debug("Start comparing \"{}\" and \"{}\".", file1.getPath(), file2.getPath());
		
		if(file1 == file2) {
			return true;
		}
		
		boolean answer = visuallyCompareJava(file1, file2);

		if(!answer) {
			logger.info("The files \"{}\" and \"{}\" are not pixel by pixel the same image. Manual comparison required.", file1.getPath(), file2.getPath());
		}
		
		logger.debug("Finish comparing \"{}\" and \"{}\".", file1.getPath(), file2.getPath());
		
		return answer;
	}
	
	/**
	 * @param file The image to check
	 * @return <code>true</code> if the image contains one or more pixels with
	 *         some percentage of transparency (Alpha)
	 */
	public final static boolean containsAlphaTransparency(final File file) {
		logger.debug("Start Alpha pixel check for {}.", file.getPath());
		
		boolean answer = false;
		for(final int pixel : getPixels(getBufferedImage(file), file)) {
			//If the alpha is 0 for both that means that the pixels are 100%
			//transparent and the color does not matter. Return false if 
			//only 1 is 100% transparent.
			if(((pixel >> 24) & 0xff) != 255) {
				logger.debug("The image contains Aplha Transparency.");
				return true;
			}
		}
		
		logger.debug("The image does not contain Aplha Transparency.");
		logger.debug("End Alpha pixel check for {}.", file.getPath());
		
		return answer;
	}
	
	private final static void handleOptimizationFailure(final Process ps, final String binaryApplicationName, final File originalFile) throws ThirdPartyBinaryNotFoundException, ImageFileOptimizationException {
		
		try(final StringWriter writer = new StringWriter();
			final InputStream is      = ps.getInputStream()) {
			try {
				IOUtils.copy(is, writer);
				final StringBuilder errorMessage = new StringBuilder("Image conversion failed with edit code: ").append(ps.exitValue()).append(". ").append(writer);
				if(ps.exitValue() == 127 /* command not found */) {
					throw new ThirdPartyBinaryNotFoundException(binaryApplicationName, "Most likely this is due to ImageMagick not being installed on the OS. On Ubuntu run \"sudo apt-get install imagemagick\".", new RuntimeException(errorMessage.toString()));
				}
				throw ImageFileOptimizationException.getInstance(originalFile, new RuntimeException(errorMessage.toString()));
			} catch (final IOException ioe) {
				logger.error("Unable to redirect error output for child process for " + originalFile, ioe);
			}
		} catch(final ThirdPartyBinaryNotFoundException tpbnfe) {
			throw tpbnfe;
		} catch(final ImageFileOptimizationException ifoe) {
			throw ifoe;
		} catch(final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Converts an image from one format to another format using Image Magic's 
	 * {@value #CONVERT_BINARY} binary. This works better than what JAVA has 
	 * built in.
	 * 
	 * @param fromImage The starting image.
	 * @param toImage The ending (converted) image.
	 * @throws InterruptedException Happens in the application is being rude.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           {@value #CONVERT_BINARY}
	 *                                           application does not exist.
	 */
	public final static void convertImageNative(final File fromImage, final File toImage) throws InterruptedException, ThirdPartyBinaryNotFoundException {
		final Process ps;
		try {
			ps = new ProcessBuilder(CONVERT_BINARY, fromImage.getCanonicalPath(), toImage.getCanonicalPath()).start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(CONVERT_BINARY, "Most likely this is due to ImageMagic not being installed on the OS. On Ubuntu run \"sudo apt-get install imagemagick\".", ioe);
		}
		
		if((ps.waitFor() != 0) || !toImage.exists()) {
			handleOptimizationFailure(ps, CONVERT_BINARY, fromImage);
		}
	}
	
	/**
	 * Checks to see if the image is an animated gif.
	 * 
	 * @param file The file to check
	 * @return <code>true</code> if it is an animated gif.
	 */
	public final static boolean isAminatedGif(final File file) {

		try(final ImageInputStream stream = ImageIO.createImageInputStream(file)) {
			if(stream == null) {
				return true;
			}
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
			if (!readers.hasNext()) {
				throw new RuntimeException("no image reader found");
			}
			final ImageReader reader = readers.next();
			reader.setInput(stream); // don't omit this line!
			return (reader.getNumImages(true) > 1); // don't use false!
		} catch (final IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}