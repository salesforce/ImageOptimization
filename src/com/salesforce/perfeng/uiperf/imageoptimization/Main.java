package com.salesforce.perfeng.uiperf.imageoptimization;

import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.GIF_EXTENSION;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.GIF_MIME_TYPE;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.JPEG_EXTENSION;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.JPEG_MIME_TYPE;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.PNG_EXTENSION;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.PNG_MIME_TYPE;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.salesforce.perfeng.uiperf.imageoptimization.dto.OptimizationResult;
import com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService;
import com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.FileTypeConversion;
import com.salesforce.perfeng.uiperf.imageoptimization.service.ImageOptimizationService;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageFileOptimizationException;

/**
 * Class to run the image optimization against a specified directory. Used 
 * primarily for testing.
 * 
 * @author eperret (Eric Perret)
 */
public class Main {

	/**
	 * The default location where the binaries exist that are used to optimize 
	 * the images.
	 */
	public final static String DEFAULT_IMAGE_OPTIMIZATION_BINARY_LOCATION;
	static {
		if ("linux".equals(System.getProperty("os.name").toLowerCase())) {
			DEFAULT_IMAGE_OPTIMIZATION_BINARY_LOCATION = "./../ImageOptimization/lib/binary/linux";
		} else {
			throw new UnsupportedOperationException("Your OS is not supported by this application. Currently only linux is supported");
		}
	}
	
	private final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	/**
	 * @param args command line args. This not used
	 * @throws ImageFileOptimizationException  Thrown if there is an issue 
	 *                                         trying to optimize one of the 
	 *                                         images.
	 * @throws IOException Can be thrown by the {@link ImageOptimizationService}
	 *                     when interacting with the passed in files.
	 */
	public static void main(final String[] args) throws ImageFileOptimizationException, IOException {
		final File tmpDir = File.createTempFile(Main.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		
		final IImageOptimizationService<Object> service = new ImageOptimizationService<>(tmpDir, new File(DEFAULT_IMAGE_OPTIMIZATION_BINARY_LOCATION));
		@SuppressWarnings("unused")
		List<OptimizationResult<Object>> list = service.optimizeAllImages(FileTypeConversion.ALL, false, new File("/home/eperret/Downloads/filters_sprite.png"));
		System.out.println("Images can be downloaded from: " + service.getFinalResultsDirectory());
	}
	
	/**
	 * Used to determine if the image's content type matches the extension.
	 * 
	 * @param contentType The content type to check (mime type)
	 * @param extension The extension to check.
	 * @return <code>true</code> if the combination is valid, otherwise 
	 *         <code>false</code>.
	 */
	private static final boolean isValidContentType(final String contentType, final String extension) {
		if(PNG_EXTENSION.equals(extension)) {
			return PNG_MIME_TYPE.equals(contentType);
		}
		if(GIF_EXTENSION.equals(extension)) {
			return GIF_MIME_TYPE.equals(contentType);
		}
		if(JPEG_EXTENSION.equals(extension)) {
			return JPEG_MIME_TYPE.equals(contentType);
		}
		return false;
	}
	
	/**
	 * Retrieves all of the valid images from the passed in directories.
	 * 
	 * @param rootDirectories The directories to search in.
	 * @return The <code>List</code> of images that were found.
	 * @throws IOException Thrown if there are any issues trying to read from 
	 *                     the file system.
	 */
	@SuppressWarnings("unused")
	private static List<File> getAllImages(final String... rootDirectories) throws IOException  {
		
		final List<File> images = new ArrayList<>();
		for(final String rootDirectory : rootDirectories) {
			logger.info("Starting with {} at {}", rootDirectory, new Date());
			InputStream is = null;
			final Collection<File> c = FileUtils.listFiles(new File(rootDirectory), IImageOptimizationService.SUPPORTED_FILE_EXTENSIONS, true);
			for(final File image : c) {
				try {
					is = new BufferedInputStream(new FileInputStream(image));
					final String contentType = URLConnection.guessContentTypeFromStream(is);
					
					if(isValidContentType(contentType, FilenameUtils.getExtension(image.getName()).toLowerCase())) {
						images.add(image);
					} else if(image.length() > 0) {
						logger.warn("Skipping file. Unexpected content type for file\n\tfile: {}\n\tcontentType: {}", image.getPath(), contentType);
					}
				} finally {
					if(is != null) {
						try {
							is.close();
						} catch (final IOException ignore) {
							// If we can't close it then there is nothing we can
							// do and should move on.
						}
					}
				}
			}

			logger.info("Done with {} at {}", rootDirectory, new Date());
			logger.info("Found {} images.", Integer.valueOf(images.size()));
		}
		
		return images;
	}
}