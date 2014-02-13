package com.salesforce.perfeng.uiperf.imageoptimization.utils;

import static org.junit.Assert.*;

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
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 3rd party binary 
	 *                                           used for optimizing images does
	 *                                           not exist.
	 */
	@Test
	public void testVisuallyCompare() throws ThirdPartyBinaryNotFoundException {

		assertTrue(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small.jpg"),
				new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_smushit.jpg")));

		assertFalse(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small.jpg"), 
				new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_jpegmini.jpg")));

		assertTrue(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/2013_summer_force.gif"), 
				new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/2013_summer_force.gif")));

		assertFalse(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_optimized.png"), 
				new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_unoptimized.png")));

		assertTrue(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif"), 
				new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif.tmp")));

		assertTrue(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/s-arrow-bo.gif"), 
				new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/s-arrow-bo2.gif")));
	}

	/**
	 * Test for {@link ImageUtils#containsAlphaTransparency(File)}.
	 */
	@Test
	public void testContainsAlphaTransparency() {
		assertFalse(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small.jpg")));
		assertFalse(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_smushit.jpg")));
		assertFalse(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_jpegmini.jpg")));
		assertFalse(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif")));
		assertFalse(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif.tmp")));
		assertTrue(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/2013_summer_force.gif")));
		assertTrue(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_optimized.png")));
		assertTrue(ImageUtils.containsAlphaTransparency(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_unoptimized.png")));
	}

	/**
	 * Test for {@link ImageUtils#getBufferedImage(File)}.
	 * 
	 * @since 188.internal
	 */
	@Test
	public void testGetBufferedImage() {

		assertNotNull(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small.jpg")));
		assertNotNull(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_smushit.jpg")));
		assertNotNull(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/sergey_reasonably_small_jpegmini.jpg")));
		assertNotNull(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif")));
		assertNotNull(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/no_transparency.gif.tmp")));
		assertNotNull(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/2013_summer_force.gif")));
		assertNotNull(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_optimized.png")));
		assertNotNull(ImageUtils.getBufferedImage(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/safe32_unoptimized.png")));
	}

	/**
	 * Test for {@link ImageUtils#convertImageNative(File, File)}.
	 * 
	 * @throws IOException Thrown if there is an issue trying to create temp 
	 *                     files.
	 * @throws InterruptedException Thrown if there is a problem trying to 
	 *                              convert the image.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 3rd party binary 
	 *                                           used for optimizing images does
	 *                                           not exist.
	 * @since 188.internal
	 */
	@Test
	public void testConvertImageNative() throws IOException, ThirdPartyBinaryNotFoundException, InterruptedException {

		final File fileToConvert = new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/forceapp_bg.gif");
		final long fileToConvertHash = FileUtils.checksumCRC32(fileToConvert);
		
		File convertedFile = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "." + IImageOptimizationService.PNG_EXTENSION);
		ImageUtils.convertImageNative(fileToConvert, convertedFile);
		
		assertTrue(convertedFile.exists());
		assertTrue(convertedFile.canRead());
		assertTrue(convertedFile.canWrite());
		assertTrue(convertedFile.isFile());
		assertFalse(convertedFile.isDirectory());
		assertFalse(convertedFile.isHidden());
		assertTrue(convertedFile.length() > 0);
		assertEquals(fileToConvertHash, FileUtils.checksumCRC32(fileToConvert));
		
		try(final InputStream is = new BufferedInputStream(new FileInputStream(convertedFile))) {
			assertEquals(IImageOptimizationService.PNG_MIME_TYPE, URLConnection.guessContentTypeFromStream(is));
		}
		assertTrue(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/forceapp_bg.gif"), convertedFile));
		
		final File tmpDir = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		tmpDir.deleteOnExit();
		
		convertedFile = new File(tmpDir.getCanonicalPath() + "/forceapp_bg." + IImageOptimizationService.PNG_EXTENSION);
		ImageUtils.convertImageNative(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/forceapp_bg.gif"), convertedFile);

		assertTrue(convertedFile.exists());
		assertTrue(convertedFile.canRead());
		assertTrue(convertedFile.canWrite());
		assertTrue(convertedFile.isFile());
		assertFalse(convertedFile.isDirectory());
		assertFalse(convertedFile.isHidden());
		assertTrue(convertedFile.length() > 0);
		assertEquals(fileToConvertHash, FileUtils.checksumCRC32(fileToConvert));

		try(final InputStream is = new BufferedInputStream(new FileInputStream(convertedFile))) {
			assertEquals(IImageOptimizationService.PNG_MIME_TYPE, URLConnection.guessContentTypeFromStream(is));
		}
		assertTrue(ImageUtils.visuallyCompare(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/utils/forceapp_bg.gif"), convertedFile));
	}


	/**
	 * Test for {@link ImageUtils#isAminatedGif(File)}.
	 */
	@Test
	public void testIsAminatedGif() {
		assertTrue(ImageUtils.isAminatedGif(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/service/loading.gif")));
		assertFalse(ImageUtils.isAminatedGif(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/service/el_icon.gif")));
		assertFalse(ImageUtils.isAminatedGif(new File("./test/com/salesforce/perfeng/uiperf/imageoptimization/service/addCol.gif")));
	}
}