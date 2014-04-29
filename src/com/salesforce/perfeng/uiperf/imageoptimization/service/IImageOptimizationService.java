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
package com.salesforce.perfeng.uiperf.imageoptimization.service;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.salesforce.perfeng.uiperf.imageoptimization.dto.OptimizationResult;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageFileOptimizationException;

/**
 * Interface for the Image Optimization Service. This service handles optimizing
 * all of the passed in images.
 * 
 * @author eperret (Eric Perret)
 * @since 186.internal
 * @param <C> object holding the changelist information.
 */
public interface IImageOptimizationService<C> {
	/**
	 * The PNG extension.
	 */
	public final static String PNG_EXTENSION = "png";
	/**
	 * The PNG mime type.
	 */
	public final static String PNG_MIME_TYPE = "image/png";
	/**
	 * The JPEG extension.
	 */
	public final static String JPEG_EXTENSION = "jpg";
	/**
	 * The JPEG mime type.
	 */
	public final static String JPEG_MIME_TYPE = "image/jpeg";
	/**
	 * The GIF extension.
	 */
	public final static String GIF_EXTENSION = "gif";
	/**
	 * The GIF mime type.
	 */
	public final static String GIF_MIME_TYPE = "image/gif";
	/**
	 * The WebP extension.
	 */
	public final static String WEBP_EXTENSION = "webp";
	/**
	 * The WebP mime type.
	 */
	public final static String WEBP_MIME_TYPE = "image/webp";
	
	/**
	 * <p>Used by the image optimization service to indicate if and how images 
	 * should be converted to other image types if it will improve performance.
	 * </p>
	 * <p>Example is GIF to PNG</p>
	 * 
	 * @author eperret (Eric Perret)
	 * @since 188.internal
	 */
	public enum FileTypeConversion {
		/**
		 * None of the images will be converted to a different files type
		 */
		NONE,
		/**
		 * There are no restrictions around which images will be converted to 
		 * different images types as long as it results in a smaller file size 
		 * (less bytes) and optimization is lossless.
		 */
		ALL,
		/**
		 * The same as {@link #ALL} except that it will not convert the image if 
		 * it is a GIF with Alpha transparency. PNG files with transparency, 
		 * when loaded in IE6, show the transparent parts as gray.
		 */
		IE6SAFE;
		
		/**
		 * Checks to see if the passed in {@link FileTypeConversion} is enabled,
		 * AKA not equal to {@link #NONE}.
		 * 
		 * @param fileTypeConversion The Object to check
		 * @return true or false
		 * @since 190.internal
		 */
		public final static boolean isEnabled(final FileTypeConversion fileTypeConversion) {
			return fileTypeConversion != NONE;
		}
	}
	
	/**
	 * The complete list of supported file extensions that the service will 
	 * optimize.
	 */
	public final static String[] SUPPORTED_FILE_EXTENSIONS = {PNG_EXTENSION, JPEG_EXTENSION, GIF_EXTENSION, PNG_EXTENSION.toUpperCase(), JPEG_EXTENSION.toUpperCase(), GIF_EXTENSION.toUpperCase()};

	/**
	 * This method will try to optimize all of the passed in images.
	 * 
	 * @param conversionType If and how to handle converting images from one 
	 *                       type to another.
	 * @param includeWebPConversion If <code>true</code> then the WebP versions 
	 *                              of the image will be generated.
	 * @param files The images to optimize
	 * @return The results from the optimization. All items in the {@link List}
	 *         are considered optimized, not <code>null</code>, and will exclude
	 *         images that could not be optimized to a smaller size.
	 * @throws ImageFileOptimizationException Thrown if there is a problem 
	 *                                        optimizing an image.
	 * @see #optimizeAllImages(FileTypeConversion, boolean, File...)
	 */
	public List<OptimizationResult<C>> optimizeAllImages(final FileTypeConversion conversionType, final boolean includeWebPConversion, final Collection<File> files) throws ImageFileOptimizationException;
	
	/**
	 * This method will try to optimize all of the passed in images.
	 * 
	 * @param conversionType If and how to handle converting images from one 
	 *                       type to another.
	 * @param includeWebPConversion If <code>true</code> then the WebP versions 
	 *                              of the image will be generated.
	 * @param files The images to optimize
	 * @return The results from the optimization. All items in the {@link List}
	 *         are considered optimized, not <code>null</code>, and will exclude
	 *         images that could not be optimized to a smaller size.
	 * @throws ImageFileOptimizationException Thrown if there is a problem 
	 *                                        optimizing an image.
	 * @see #optimizeAllImages(FileTypeConversion, boolean, Collection)
	 */
	public List<OptimizationResult<C>> optimizeAllImages(final FileTypeConversion conversionType, final boolean includeWebPConversion, final File... files) throws ImageFileOptimizationException;
	
	/**
	 * Returns the path to the optimized images.
	 * 
	 * @return The fully qualified path.
	 */
	public abstract String getFinalResultsDirectory();
}