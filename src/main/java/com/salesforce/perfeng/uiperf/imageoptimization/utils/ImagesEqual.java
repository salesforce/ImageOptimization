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

import static com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageUtils.getBufferedImage;
import static com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageUtils.getPixels;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author eperret (Eric Perret)
 *
 */
public final class ImagesEqual {
	
	private final static Logger logger = LoggerFactory.getLogger(ImagesEqual.class);

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
		
		if(file1 == null || file2 == null) {
			throw new IllegalArgumentException("The passed in files cannot be null.");
		}
		if(!file1.canRead()) {
			throw new IllegalArgumentException("The passed in file, \"" + file1.getPath() + "\", cannot be read or does not exist.");
		}
		if(!file2.canRead()) {
			throw new IllegalArgumentException("The passed in file, \"" + file2.getPath() + "\", cannot be read or does not exist.");
		}
		
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
}
