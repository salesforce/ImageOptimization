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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.salesforce.perfeng.uiperf.ThirdPartyBinaryNotFoundException;
import com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService;
import com.salesforce.perfeng.uiperf.imageoptimization.service.ImageOptimizationServiceTest;

/**
 * @author eperret (Eric Perret)
 * @since 186.internal
 */
public class ImageUtilsTest {

    /**
     * Test for {@link ImageUtils#visuallyCompare(File, File)}.
     * 
     * @throws ThirdPartyBinaryNotFoundException Thrown if the 3rd party binary used for optimizing images does not
     *         exist.
     */
    @SuppressWarnings({ "boxing", "static-method" })
	@Test
    public void testVisuallyCompare() throws ThirdPartyBinaryNotFoundException {

        assertThat(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small.jpg"),
                new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_smushit.jpg")), equalTo(true));

        assertThat(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small.jpg"), 
                new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_jpegmini.jpg")), equalTo(false));

        assertThat(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/2013_summer_force.gif"), 
                new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/2013_summer_force.gif")), equalTo(true));

        assertThat(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_optimized.png"), 
                new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_unoptimized.png")), equalTo(false));

        assertThat(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif"), 
                new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif.tmp")), equalTo(true));

        assertThat(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/s-arrow-bo.gif"), 
                new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/s-arrow-bo2.gif")), equalTo(true));
    }

    /**
     * Test for {@link ImageUtils#containsAlphaTransparency(File)}.
     */
    @SuppressWarnings({ "static-method", "boxing" })
    @Test
    public void testContainsAlphaTransparency() {
    	assertThat(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small.jpg")), equalTo(false));
    	assertThat(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_smushit.jpg")), equalTo(false));
    	assertThat(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_jpegmini.jpg")), equalTo(false));
    	assertThat(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif")), equalTo(false));
    	assertThat(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif.tmp")), equalTo(false));
    	assertThat(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/2013_summer_force.gif")), equalTo(true));
    	assertThat(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_optimized.png")), equalTo(true));
    	assertThat(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_unoptimized.png")), equalTo(true));
    }

    /**
     * Test for {@link ImageUtils#getBufferedImage(File)}.
     * 
     * @since 188.internal
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetBufferedImage() {

    	assertThat(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small.jpg")), notNullValue());
    	assertThat(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_smushit.jpg")), notNullValue());
    	assertThat(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_jpegmini.jpg")), notNullValue());
        assertThat(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif")), notNullValue());
        assertThat(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif.tmp")), notNullValue());
        assertThat(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/2013_summer_force.gif")), notNullValue());
        assertThat(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_optimized.png")), notNullValue());
        assertThat(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_unoptimized.png")), notNullValue());
    }

    /**
     * Test for {@link ImageUtils#convertImageNative(File, File)}.
     * 
     * @throws IOException Thrown if there is an issue trying to create temp files.
     * @throws InterruptedException Thrown if there is a problem trying to convert the image.
     * @throws ThirdPartyBinaryNotFoundException Thrown if the 3rd party binary used for optimizing images does not
     *            exist.
     * @since 188.internal
     */
    @SuppressWarnings({ "static-method", "boxing" })
    @Test
    public void testConvertImageNative() throws IOException, ThirdPartyBinaryNotFoundException, InterruptedException {

        final File fileToConvert = new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/forceapp_bg.gif");
        final long fileToConvertHash = FileUtils.checksumCRC32(fileToConvert);
        
        File convertedFile = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "." + IImageOptimizationService.PNG_EXTENSION);
        ImageUtils.convertImageNative(fileToConvert, convertedFile);
        
        assertThat(convertedFile.exists(), equalTo(true));
        assertThat(convertedFile.canRead(), equalTo(true));
        assertThat(convertedFile.canWrite(), equalTo(true));
        assertThat(convertedFile.isFile(), equalTo(true));
        assertThat(convertedFile.isDirectory(), equalTo(false));
        assertThat(convertedFile.isHidden(), equalTo(false));
        assertThat(convertedFile.length(), greaterThan(0L));
        assertThat(FileUtils.checksumCRC32(fileToConvert), equalTo(fileToConvertHash));
        
        try(final InputStream is = new BufferedInputStream(new FileInputStream(convertedFile))) {
            assertEquals(IImageOptimizationService.PNG_MIME_TYPE, URLConnection.guessContentTypeFromStream(is));
        }
        assertThat(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/forceapp_bg.gif"), convertedFile), equalTo(true));
        
        final File tmpDir = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "");
        tmpDir.delete();
        tmpDir.mkdir();
        tmpDir.deleteOnExit();
        
        convertedFile = new File(tmpDir.getCanonicalPath() + "/forceapp_bg." + IImageOptimizationService.PNG_EXTENSION);
        ImageUtils.convertImageNative(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/forceapp_bg.gif"), convertedFile);

        assertThat(convertedFile.exists(), equalTo(true));
        assertThat(convertedFile.canRead(), equalTo(true));
        assertThat(convertedFile.canWrite(), equalTo(true));
        assertThat(convertedFile.isFile(), equalTo(true));
        assertThat(convertedFile.isDirectory(), equalTo(false));
        assertThat(convertedFile.isHidden(), equalTo(false));
        assertThat(convertedFile.length(), greaterThan(0L));
        assertThat(FileUtils.checksumCRC32(fileToConvert), equalTo(fileToConvertHash));

        try(final InputStream is = new BufferedInputStream(new FileInputStream(convertedFile))) {
            assertThat(URLConnection.guessContentTypeFromStream(is), equalTo(IImageOptimizationService.PNG_MIME_TYPE));
        }
        assertThat(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/forceapp_bg.gif"), convertedFile), equalTo(true));
    }

    /**
     * Test for {@link ImageUtils#isAminatedGif(File)}.
     */
    @SuppressWarnings({ "static-method", "boxing" })
    @Test
    public void testIsAminatedGif() {
        assertThat(ImageUtils.isAminatedGif(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/service/loading.gif")), equalTo(true));
        assertThat(ImageUtils.isAminatedGif(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/service/el_icon.gif")), equalTo(false));
        assertThat(ImageUtils.isAminatedGif(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/service/addCol.gif")), equalTo(false));
    }
}