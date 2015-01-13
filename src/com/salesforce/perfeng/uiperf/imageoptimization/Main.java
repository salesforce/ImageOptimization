/*******************************************************************************
 * Copyright (c) 2014, Salesforce.com, Inc.
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
package com.salesforce.perfeng.uiperf.imageoptimization;

import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.GIF_EXTENSION;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.GIF_MIME_TYPE;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.JPEG_EXTENSION;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.JPEG_EXTENSION2;
import static com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.JPEG_EXTENSION3;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.annotation.Immutable;
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
@Immutable
public class Main {

	/**
	 * The location where the binaries exist that are used to optimize the 
	 * images.
	 */
	public final static String IMAGE_OPTIMIZATION_BINARY_LOCATION;
	static {
		if ("linux".equals(System.getProperty("os.name").toLowerCase())) {
			IMAGE_OPTIMIZATION_BINARY_LOCATION = System.getProperty("binariesDirectory");
			if(IMAGE_OPTIMIZATION_BINARY_LOCATION == null || IMAGE_OPTIMIZATION_BINARY_LOCATION.isEmpty()) {
				throw new IllegalArgumentException("Missing location for the image optimization binaries. From the commandline add \"-DbinariesDirectory=<PATH_TO_BINARIES_DIRECTORY>\"");
			}
		} else {
			throw new UnsupportedOperationException("Your OS is not supported by this application. Currently only linux is supported");
		}
	}
	
	private final static Logger logger = LoggerFactory.getLogger(Main.class);
	
	/**
	 * Optimizes all of the images that are passed in.
	 * 
	 * @param args Command line args. Each arg is the path different to an image
	 *             or directory of images to optimize.
	 * @throws ImageFileOptimizationException  Thrown if there is an issue 
	 *                                         trying to optimize one of the 
	 *                                         images.
	 * @throws IOException Can be thrown by the {@link ImageOptimizationService}
	 *                     when interacting with the passed in files.
	 * @throws TimeoutException Thrown if it takes to long to optimize an image.
	 */
	public static void main(final String[] args) throws ImageFileOptimizationException, IOException, TimeoutException {
		
		if(args.length == 0) {
			logger.warn("Missing main method arguments. No files to optimize.");
			return;
		}
		
		final Set<File> imagesToOptimize = new TreeSet<>();
		File file;
		for(final String path : args) {
			file = new File(path);
			if(!file.exists()) {
				throw new IllegalArgumentException("The file \"" + path + "\" does not exist.");
			} else if(file.isFile()) {
				imagesToOptimize.add(file);
			} else {
				imagesToOptimize.addAll(getAllImages(path));
			}
		}
		
		final IImageOptimizationService<Void> service = ImageOptimizationService.createInstance(IMAGE_OPTIMIZATION_BINARY_LOCATION, 0);
		final List<OptimizationResult<Void>> list = service.optimizeAllImages(FileTypeConversion.ALL, false, imagesToOptimize);
		System.out.println(list);
		System.out.println("Images can be downloaded from: " + service.getFinalResultsDirectory());
	}
	
	/**
	 * Used to determine if the image's content type matches the extension.
	 * 
	 * @param contentType The content type to check (mime type)
	 * @param extension The extension to check.
	 * @return {@code true} if the combination is valid, otherwise 
	 *         {@code false}.
	 */
	private static final boolean isValidContentType(final String contentType, final String extension) {
		if(PNG_EXTENSION.equals(extension)) {
			return PNG_MIME_TYPE.equals(contentType);
		}
		if(GIF_EXTENSION.equals(extension)) {
			return GIF_MIME_TYPE.equals(contentType);
		}
		if(JPEG_EXTENSION.equals(extension) || JPEG_EXTENSION2.equals(extension) || JPEG_EXTENSION3.equals(extension)) {
			return JPEG_MIME_TYPE.equals(contentType);
		}
		return false;
	}
	
	/**
	 * Retrieves all of the valid images from the passed in directories.
	 * 
	 * @param rootDirectories The directories to search in.
	 * @return The {@link List} of images that were found.
	 * @throws IOException Thrown if there are any issues trying to read from 
	 *                     the file system.
	 */
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