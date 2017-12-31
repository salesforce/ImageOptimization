/*******************************************************************************
 * Copyright (c) 2017, Salesforce.com, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this 
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * Neither the name of Salesforce.com nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.salesforce.perfeng.uiperf.imageoptimization.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
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
 * Utility methods used to interact with images. This class is immutable and threadsafe.
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
	
	/**
	 * Gets the {@link BufferedImage} from the passed in {@link File}.
	 * 
	 * @param file The <code>File</code> to use.
	 * @return The resulting <code>BufferedImage</code>
	 */
	@SuppressWarnings("unused")
	final static BufferedImage getBufferedImage(File file) {
		
		Image image;
		
		try {
			// ImageIO.read(file) is broken for some images so I went this 
			// route
			image = Toolkit.getDefaultToolkit().createImage(file.getCanonicalPath());
			
			//forces the image to be rendered
			new ImageIcon(image);
		} catch(final Exception e2) {
			throw new ImageFileOptimizationException(file.getPath(), e2);
		}

		final BufferedImage converted;
		if((image != null) && (image.getWidth(null) > 0) && (image.getHeight(null) > 0)) {
			converted = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2d = converted.createGraphics();
	        g2d.drawImage(image, 0, 0, null);
	        g2d.dispose();
		} else {
			try {
				converted = ImageIO.read(file);
			} catch(final Exception e2) {
				throw new ImageFileOptimizationException(file.getPath(), e2);
			}
		}
		return converted;
	}
	
	/**
	 * Gets the array of pixels from the passed in {@link BufferedImage}.
	 * 
	 * @param img The image to get the pixels from
	 * @param file The file containing the image
	 * @return An array containing the pixels
	 */
	static final int[] getPixels(final BufferedImage img, final File file) {
		
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
	 * @param file The image to check
	 * @return {@code true} if the image contains one or more pixels with some percentage of transparency (Alpha)
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
				IOUtils.copy(is, writer, Charset.defaultCharset());
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
			throw new ThirdPartyBinaryNotFoundException(CONVERT_BINARY, "Most likely this is due to ImageMagick not being installed on the OS. On Ubuntu run, \"sudo apt-get install imagemagick\".", ioe);
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