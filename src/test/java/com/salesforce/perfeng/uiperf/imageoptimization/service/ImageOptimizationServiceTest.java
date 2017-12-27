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
package com.salesforce.perfeng.uiperf.imageoptimization.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.io.FileMatchers.aFileNamed;
import static org.hamcrest.io.FileMatchers.aFileWithSize;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.io.FileMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.salesforce.perfeng.uiperf.imageoptimization.dto.OptimizationResult;
import com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.FileTypeConversion;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.FixedFileUtils;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageFileOptimizationException;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageUtils;

/**
 * Test for {@link ImageOptimizationService}.
 * 
 * @author eperret (Eric Perret)
 * @since 186.internal
 */
public class ImageOptimizationServiceTest {
	
	private static final String WEBP_ID = "|webp";
	private static String defaultBinaryAppLocation;
	
	private ImageOptimizationService<Object> imageOptimizationService;
	
	/**
	 * Used to initialize the default location of the Linux binaries used by all of the tests.
	 */
	@BeforeAll
	public static final void beforeClass() {
		if ("linux".equals(System.getProperty("os.name").toLowerCase())) {
			defaultBinaryAppLocation = "./lib/binary/linux/";
		} else {
			throw new UnsupportedOperationException("Your OS is not supported by this application. Currently only linux is supported.");
		}
	}
	
	/**
	 * Used to initialize the {@link ImageOptimizationService} used by all of the tests.
	 * 
	 * @throws IOException Thrown if there is a problem trying to initialize the
	 *                     directories used by 
	 *                     {@link ImageOptimizationService#ImageOptimizationService(File, File)}.
	 */
	@BeforeEach
    public void setUp() throws IOException {
		final File tmpDir = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		tmpDir.deleteOnExit();
		
		imageOptimizationService = new ImageOptimizationService<>(tmpDir, new File(defaultBinaryAppLocation));
    }
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File)} to test that files cause an
	 * error.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationServicePassInFile() {
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(File.createTempFile("qqq", "qqq"), new File(defaultBinaryAppLocation)); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File)} to test that files cause an
	 * error.
	 * 
	 * @throws IOException Can be thrown by the {@code ImageOptimizationService} constructor if its passed in file has
	 *         an issue.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationServicePassInFile2() throws IOException {
		final File file = File.createTempFile("qqq", "qqq");
		file.createNewFile();
		file.deleteOnExit();
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(file, new File(defaultBinaryAppLocation)); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File)} to test that files cause an
	 * error.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationServicePassInNullFolder() {
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(null, new File(defaultBinaryAppLocation)); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File)}
	 * 
	 * @throws IOException Can be thrown by the {@code ImageOptimizationService} constructor if its passed in file has
	 *         an issue.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testImageOptimizationService() throws IOException {
		final File tmpDir = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		tmpDir.deleteOnExit();
		assertThat(new ImageOptimizationService<>(tmpDir, new File(defaultBinaryAppLocation)), notNullValue());
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File, int)} to test that files cause an
	 * error.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationService2PassInFile() {
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(File.createTempFile("qqq", "qqq"), new File(defaultBinaryAppLocation), 1); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File, int)} to test that files cause an
	 * error.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationService2ZeroPassInFile() {
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(File.createTempFile("qqq", "qqq"), new File(defaultBinaryAppLocation), 0); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File, int)} to test that files cause an
	 * error.
	 * 
	 * @throws IOException Can be thrown by the {@code ImageOptimizationService} constructor if its passed in file has
	 *         an issue.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationService2PassInFile2() throws IOException {
		final File file = File.createTempFile("qqq", "qqq");
		file.createNewFile();
		file.deleteOnExit();
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(file, new File(defaultBinaryAppLocation), 1); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File, int)} to test that files cause an
	 * error.
	 * 
	 * @throws IOException Can be thrown by the {@code ImageOptimizationService} constructor if its passed in file has
	 *         an issue.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationService2ZeroPassInFile2() throws IOException {
		final File file = File.createTempFile("qqq", "qqq");
		file.createNewFile();
		file.deleteOnExit();
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(file, new File(defaultBinaryAppLocation), 0); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File, int)} to test that files cause an
	 * error.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationService2PassInNullFolder() {
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(null, new File(defaultBinaryAppLocation), 1); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File, int)} to test that files cause an
	 * error.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testImageOptimizationService2ZeroPassInNullFolder() {
		assertThrows(IllegalArgumentException.class, () -> { new ImageOptimizationService<>(null, new File(defaultBinaryAppLocation), 0); });
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File, int)}
	 * 
	 * @throws IOException Can be thrown by the {@code ImageOptimizationService} constructor if its passed in file has
	 *         an issue.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testImageOptimizationService2() throws IOException {
		final File tmpDir = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		tmpDir.deleteOnExit();
		assertThat(new ImageOptimizationService<>(tmpDir, new File(defaultBinaryAppLocation), 1), notNullValue());
	}
	
	/**
	 * Test method for {@link ImageOptimizationService#ImageOptimizationService(File, File, int)}
	 * 
	 * @throws IOException Can be thrown by the {@code ImageOptimizationService} constructor if its passed in file has
	 *         an issue.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testImageOptimizationService2Zero() throws IOException {
		final File tmpDir = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		tmpDir.deleteOnExit();
		assertThat(new ImageOptimizationService<>(tmpDir, new File(defaultBinaryAppLocation), 0), notNullValue());
	}

	private static final void validateFileOptimization(final OptimizationResult<Object> result, final ImageOptimizationTestDTO imageOptimizationTestDTO, final boolean isWebP) throws IOException {
		final String errorMsg = String.format("failed for image \"%s\"", imageOptimizationTestDTO.getMasterFile().getName());
		
		// Checking that the master image was not updated as part of this 
		// process.
		assertThat(errorMsg, Long.valueOf(FileUtils.checksumCRC32(imageOptimizationTestDTO.getMasterFile())), equalTo(Long.valueOf(imageOptimizationTestDTO.getMasterFileChecksum())));
		
		if(imageOptimizationTestDTO.isOptimized() || isWebP) {

			assertThat(errorMsg, result, notNullValue());
			
			assertThat(errorMsg, result.getGusBugId(), nullValue());
			assertThat(errorMsg, result.getNewChangeList(), nullValue());
			assertThat(errorMsg, result.getOptimizedFile(), notNullValue());
			assertThat(errorMsg, Boolean.valueOf(result.getOptimizedFile().exists()), equalTo(Boolean.TRUE));
			
			if(isWebP) {
				assertThat(errorMsg, FilenameUtils.removeExtension(result.getOptimizedFile().getName()), equalTo(FilenameUtils.removeExtension(imageOptimizationTestDTO.getMasterFile().getName())));
				assertThat(errorMsg, FilenameUtils.getExtension(result.getOptimizedFile().getName()), equalTo(IImageOptimizationService.WEBP_EXTENSION));
				assertThat(errorMsg, Boolean.valueOf(result.isBrowserSpecific()), equalTo(Boolean.TRUE));
				assertThat(errorMsg, Boolean.valueOf(result.isFileTypeChanged()), equalTo(Boolean.TRUE));
			} else if(imageOptimizationTestDTO.isFileTypeChanged()) {
				assertThat(errorMsg, FilenameUtils.removeExtension(result.getOptimizedFile().getName()), equalTo(FilenameUtils.removeExtension(imageOptimizationTestDTO.getMasterFile().getName())));
				assertThat(errorMsg, FilenameUtils.getExtension(imageOptimizationTestDTO.getMasterFile().getName()), equalTo(IImageOptimizationService.GIF_EXTENSION));
				assertThat(errorMsg, FilenameUtils.getExtension(result.getOptimizedFile().getName()), equalTo(IImageOptimizationService.PNG_EXTENSION));
				assertThat(errorMsg, Boolean.valueOf(result.isFileTypeChanged()), equalTo(Boolean.TRUE));
			} else {
				assertThat(errorMsg, result.getOptimizedFile().getName(), equalTo(imageOptimizationTestDTO.getMasterFile().getName()));
				assertThat(errorMsg, Boolean.valueOf(result.isBrowserSpecific()), equalTo(Boolean.FALSE));
				assertThat(errorMsg, Boolean.valueOf(result.isFileTypeChanged()), equalTo(Boolean.FALSE));
			}
			
			assertThat(errorMsg, Long.valueOf(result.getOptimizedFileSize()), equalTo(Long.valueOf(result.getOptimizedFile().length())));
			assertThat(errorMsg, result.getOriginalFile(), notNullValue());
			assertThat(errorMsg, Boolean.valueOf(result.getOriginalFile().exists()), equalTo(Boolean.TRUE));
			assertThat(errorMsg, result.getOriginalFile(), equalTo(imageOptimizationTestDTO.getMasterFile().getCanonicalFile()));
			assertThat(errorMsg, Long.valueOf(result.getOriginalFileSize()), equalTo(Long.valueOf(imageOptimizationTestDTO.getMasterFile().length())));
			
			//The assert is flappy for animated gifs.
			if(!imageOptimizationTestDTO.isAnimatedGif()) {
				assertThat(errorMsg, Boolean.valueOf(result.isFailedAutomatedTest()), equalTo(Boolean.valueOf(imageOptimizationTestDTO.isFailedAutomatedTest())));
			}
			assertThat(errorMsg, Boolean.valueOf(result.isOptimized()), equalTo(Boolean.TRUE));
		} else {
			assertThat(errorMsg, result, nullValue());
		}
	}
	
	private static final File getTempDir() throws IOException {
		final File tmpDir = File.createTempFile(ImageOptimizationServiceTest.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		tmpDir.deleteOnExit();
		return tmpDir;
	}
	
	private static final int getNumberOfWebPCompatibleImages(final ImageOptimizationTestDTO[] imageOptimizationTestDTOList) {
		int count = 0;
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			if(!imageOptimizationTestDTO.isAnimatedGif() && !imageOptimizationTestDTO.isJPEG()) {
				count++;
			}
		}
		return count;
	}
	
	private static final int getNumberOfOptimizedImages(final ImageOptimizationTestDTO[] imageOptimizationTestDTOList) {
		int count = 0;
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			if(imageOptimizationTestDTO.isOptimized()) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Test for 
	 * {@link ImageOptimizationService#optimizeAllImages(FileTypeConversion, boolean, java.util.Collection)}.
	 * 
	 * @throws IOException can be thrown by the 
	 *                     <code>ImageOptimizationService</code> constructor or 
	 *                     when creating a temp file used in the test
	 * @throws IOException Thrown if there is an issue reading from the file 
	 *                     system.
	 * @throws TimeoutException Thrown if it takes to long to optimize an image.
	 * @throws ImageFileOptimizationException Thrown if there is an error trying
	 *                                        to optimize an image.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testOptimizeAllImagesALL() throws IOException, ImageFileOptimizationException, TimeoutException {
		
		final ImageOptimizationTestDTO[] imageOptimizationTestDTOList = {new ImageOptimizationTestDTO("csv_120.png", false, false, true),
				                                                         new ImageOptimizationTestDTO("sharing_model2.jpg", false, false, true),
				                                                         new ImageOptimizationTestDTO("loading.gif", false, false, true),
				                                                         new ImageOptimizationTestDTO("el_icon.gif", false, true, true),
				                                                         new ImageOptimizationTestDTO("safe32.png", false, false, true),
				                                                         new ImageOptimizationTestDTO("no_transparency.gif", false, true, true),
				                                                         new ImageOptimizationTestDTO("doctype_16_sprite.png", false, false, false),
				                                                         new ImageOptimizationTestDTO("addCol.gif", false, true, true),
				                                                         new ImageOptimizationTestDTO("s-arrow-bo.gif", false, true, true)};
		
		final int numberOfOptimizedImages = getNumberOfOptimizedImages(imageOptimizationTestDTOList);
		
		final List<File> filesToOptimize = new ArrayList<>(imageOptimizationTestDTOList.length);
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			filesToOptimize.add(imageOptimizationTestDTO.getMasterFile());
		}
		
		//Testing with ALL and no WebP
		List<OptimizationResult<Object>> results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.ALL, false, filesToOptimize);
		assertThat(results, notNullValue());
		
		Map<String, OptimizationResult<Object>> treasureMap = new HashMap<>(numberOfOptimizedImages);
		for(final OptimizationResult<Object> result : results) {
			assertThat(result, notNullValue());
			treasureMap.put(result.getOriginalFile().getName(), result);
		}
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName()), imageOptimizationTestDTO, false);
		}
		assertThat(results, hasSize(numberOfOptimizedImages));
		assertThat(treasureMap, aMapWithSize(numberOfOptimizedImages));
		
		//Testing with ALL and YES WebP
		final int numberOfResultImages = numberOfOptimizedImages + getNumberOfWebPCompatibleImages(imageOptimizationTestDTOList);
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.ALL, true, filesToOptimize);
		assertThat(results, notNullValue());
		
		treasureMap = new HashMap<>(numberOfResultImages);
		for(final OptimizationResult<Object> result : results) {
			assertThat(result, notNullValue());
			if(FilenameUtils.isExtension(result.getOptimizedFile().getName(), IImageOptimizationService.WEBP_EXTENSION)) {
				treasureMap.put(result.getOriginalFile().getName() + WEBP_ID, result);
			} else {
				treasureMap.put(result.getOriginalFile().getName(), result);
			}
		}
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName()), imageOptimizationTestDTO, false);
		}
		assertThat(results, hasSize(numberOfResultImages));
		assertThat(treasureMap, aMapWithSize(numberOfResultImages));
		
		//WebP Check
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			if(imageOptimizationTestDTO.isJPEG()) {
				//JPEG is not converted to WEBP
				assertThat(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), nullValue());
			} else if(imageOptimizationTestDTO.isAnimatedGif()) {
				//Animated GIF is not converted to WEBP
				assertThat(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), nullValue());
			} else {
				validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), imageOptimizationTestDTO, true);
			}
		}
		
		//Testing a null list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.ALL, false, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.ALL, true, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		//Testing an empty list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.ALL, false, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.ALL, true, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
	}
	
	/**
	 * Test for 
	 * {@link ImageOptimizationService#optimizeAllImages(FileTypeConversion, boolean, java.util.Collection)}.
	 * 
	 * @throws IOException can be thrown by the 
	 *                     <code>ImageOptimizationService</code> constructor or 
	 *                     when creating a temp file used in the test.
	 * @throws IOException Thrown if there is an issue reading from the file 
	 *                     system.
	 * @throws TimeoutException Thrown if it takes to long to optimize an image.
	 * @throws ImageFileOptimizationException Thrown if there is an error trying
	 *                                        to optimize an image.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testOptimizeAllImagesNONE() throws IOException, ImageFileOptimizationException, TimeoutException {
		
		final ImageOptimizationTestDTO[] imageOptimizationTestDTOList = {new ImageOptimizationTestDTO("csv_120.png", false, false, true),
                new ImageOptimizationTestDTO("sharing_model2.jpg", false, false, true),
                new ImageOptimizationTestDTO("loading.gif", false, false, true),
                new ImageOptimizationTestDTO("el_icon.gif", false, false, false),
                new ImageOptimizationTestDTO("safe32.png", false, false, true),
                new ImageOptimizationTestDTO("no_transparency.gif", false, false, true),
                new ImageOptimizationTestDTO("doctype_16_sprite.png", false, false, false),
                new ImageOptimizationTestDTO("addCol.gif", false, false, false),
                new ImageOptimizationTestDTO("s-arrow-bo.gif", false, false, true)};

		final int numberOfOptimizedImages = getNumberOfOptimizedImages(imageOptimizationTestDTOList);
		
		final List<File> filesToOptimize = new ArrayList<>(imageOptimizationTestDTOList.length);
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			filesToOptimize.add(imageOptimizationTestDTO.getMasterFile());
		}
		
		//Testing with NONE
		List<OptimizationResult<Object>> results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.NONE, false, filesToOptimize);
		assertThat(results, notNullValue());
		
		Map<String, OptimizationResult<Object>> treasureMap = new HashMap<>(numberOfOptimizedImages);
		for(final OptimizationResult<Object> result : results) {
			assertThat(result, notNullValue());
			treasureMap.put(result.getOriginalFile().getName(), result);
		}
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName()), imageOptimizationTestDTO, false);
		}
		assertThat(results, hasSize(numberOfOptimizedImages));
		assertThat(treasureMap, aMapWithSize(numberOfOptimizedImages));
		
		//Testing with NONE and YES WebP
		final int numberOfResultImages = numberOfOptimizedImages + getNumberOfWebPCompatibleImages(imageOptimizationTestDTOList);
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.NONE, true, filesToOptimize);
		assertThat(results, notNullValue());
		
		treasureMap = new HashMap<>(numberOfResultImages);
		for(final OptimizationResult<Object> result : results) {
			assertThat(result, notNullValue());
			if(FilenameUtils.isExtension(result.getOptimizedFile().getName(), IImageOptimizationService.WEBP_EXTENSION)) {
				treasureMap.put(result.getOriginalFile().getName() + WEBP_ID, result);
			} else {
				treasureMap.put(result.getOriginalFile().getName(), result);
			}
		}
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName()), imageOptimizationTestDTO, false);
		}
		assertThat(results, hasSize(numberOfResultImages));
		assertThat(treasureMap, aMapWithSize(numberOfResultImages));
		
		//WebP Check
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			if(imageOptimizationTestDTO.isJPEG()) {
				//JPEG is not converted to WEBP
				assertThat(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), nullValue());
			} else if(imageOptimizationTestDTO.isAnimatedGif()) {
				//Animated GIF is not converted to WEBP
				assertThat(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), nullValue());
			} else {
				validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), imageOptimizationTestDTO, true);
			}
		}
		
		//Testing a null list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.NONE, false, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.NONE, true, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		//Testing an empty list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.NONE, false, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.NONE, true, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
	}
	
	/**
	 * Test for 
	 * {@link ImageOptimizationService#optimizeAllImages(FileTypeConversion, boolean, java.util.Collection)}.
	 * 
	 * @throws IOException can be thrown by the 
	 *                     <code>ImageOptimizationService</code> constructor or 
	 *                     when creating a temp file used in the test.
	 * @throws IOException Thrown if there is an issue reading from the file 
	 *                     system.
	 * @throws TimeoutException Thrown if it takes to long to optimize an image.
	 * @throws ImageFileOptimizationException Thrown if there is an error trying
	 *                                        to optimize an image.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testOptimizeAllImagesIE6SAFE() throws IOException, ImageFileOptimizationException, TimeoutException {
		
		final ImageOptimizationTestDTO[] imageOptimizationTestDTOList = {new ImageOptimizationTestDTO("csv_120.png", false, false, true),
                new ImageOptimizationTestDTO("sharing_model2.jpg", false, false, true),
                new ImageOptimizationTestDTO("loading.gif", false, false, true),
                new ImageOptimizationTestDTO("el_icon.gif", false, false, false),
                new ImageOptimizationTestDTO("safe32.png", false, false, true),
                new ImageOptimizationTestDTO("no_transparency.gif", false, true, true),
                new ImageOptimizationTestDTO("doctype_16_sprite.png", false, false, false),
                new ImageOptimizationTestDTO("addCol.gif", false, false, false),
                new ImageOptimizationTestDTO("s-arrow-bo.gif", false, false, true)};

		final int numberOfOptimizedImages = getNumberOfOptimizedImages(imageOptimizationTestDTOList);
		
		final List<File> filesToOptimize = new ArrayList<>(imageOptimizationTestDTOList.length);
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			filesToOptimize.add(imageOptimizationTestDTO.getMasterFile());
		}
		
		//Testing with IE6SAFE
		List<OptimizationResult<Object>> results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.IE6SAFE, false, filesToOptimize);
		assertThat(results, notNullValue());
		
		Map<String, OptimizationResult<Object>> treasureMap = new HashMap<>(numberOfOptimizedImages);
		for(final OptimizationResult<Object> result : results) {
			assertThat(result, notNullValue());
			treasureMap.put(result.getOriginalFile().getName(), result);
		}
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName()), imageOptimizationTestDTO, false);
		}
		
		assertThat(treasureMap, aMapWithSize(numberOfOptimizedImages));
		assertThat(results, hasSize(numberOfOptimizedImages));
		
		//Testing with IE6SAFE and YES WebP
		final int numberOfResultImages = numberOfOptimizedImages + getNumberOfWebPCompatibleImages(imageOptimizationTestDTOList);
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.IE6SAFE, true, filesToOptimize);
		assertThat(results, notNullValue());
		
		treasureMap = new HashMap<>(numberOfResultImages);
		for(final OptimizationResult<Object> result : results) {
			assertThat(result, notNullValue());
			if(FilenameUtils.isExtension(result.getOptimizedFile().getName(), IImageOptimizationService.WEBP_EXTENSION)) {
				treasureMap.put(result.getOriginalFile().getName() + WEBP_ID, result);
			} else {
				treasureMap.put(result.getOriginalFile().getName(), result);
			}
		}
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName()), imageOptimizationTestDTO, false);
		}
		assertThat(treasureMap, aMapWithSize(numberOfResultImages));
		assertThat(results, hasSize(numberOfResultImages));
		
		//WebP Check
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			if(imageOptimizationTestDTO.isJPEG()) {
				//JPEG is not converted to WEBP
				assertThat(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), nullValue());
			} else if(imageOptimizationTestDTO.isAnimatedGif()) {
				//Animated GIF is not converted to WEBP
				assertThat(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), nullValue());
			} else {
				validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), imageOptimizationTestDTO, true);
			}
		}
		
		//Testing a null list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.IE6SAFE, false, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.IE6SAFE, true, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		//Testing an empty list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.IE6SAFE, false, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.IE6SAFE, true, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
	}
	
	/**
	 * Test for 
	 * {@link ImageOptimizationService#optimizeAllImages(FileTypeConversion, boolean, java.util.Collection)}
	 * with timeout set.
	 * 
	 * @throws IOException can be thrown by the 
	 *                     <code>ImageOptimizationService</code> constructor or 
	 *                     when creating a temp file used in the test.
	 * @throws IOException Thrown if there is an issue reading from the file 
	 *                     system.
	 * @throws ImageFileOptimizationException Thrown if there is an error trying
	 *                                        to optimize an image.
	 * @throws TimeoutException Thrown if optimizing an image timed out.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testOptimizeAllImagesNONEWithTimeoutFailure() throws IOException, ImageFileOptimizationException, TimeoutException {
		
		final ImageOptimizationTestDTO[] imageOptimizationTestDTOList = {new ImageOptimizationTestDTO("csv_120.png", false, false, true),
                new ImageOptimizationTestDTO("sharing_model2.jpg", false, false, true),
                new ImageOptimizationTestDTO("loading.gif", false, false, true),
                new ImageOptimizationTestDTO("el_icon.gif", false, false, true),
                new ImageOptimizationTestDTO("safe32.png", false, false, true),
                new ImageOptimizationTestDTO("no_transparency.gif", false, false, true),
                new ImageOptimizationTestDTO("doctype_16_sprite.png", false, false, false),
                new ImageOptimizationTestDTO("addCol.gif", false, false, false),
                new ImageOptimizationTestDTO("s-arrow-bo.gif", false, false, true),
                new ImageOptimizationTestDTO("imagebomb.png", false, false, true)};
		
		final List<File> filesToOptimize = new ArrayList<>(imageOptimizationTestDTOList.length);
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			filesToOptimize.add(imageOptimizationTestDTO.getMasterFile());
		}

		//Testing with NONE
		assertThrows(TimeoutException.class, () -> { new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 1).optimizeAllImages(FileTypeConversion.NONE, false, filesToOptimize); });

		//Testing with NONE and YES WebP
		assertThrows(TimeoutException.class, () -> { new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 1).optimizeAllImages(FileTypeConversion.NONE, true, filesToOptimize); });
		
		List<OptimizationResult<Object>> results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation)).optimizeAllImages(FileTypeConversion.NONE, false, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 1).optimizeAllImages(FileTypeConversion.NONE, true, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		//Testing an empty list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 1).optimizeAllImages(FileTypeConversion.NONE, false, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 1).optimizeAllImages(FileTypeConversion.NONE, true, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
	}
	
	/**
	 * Test for 
	 * {@link ImageOptimizationService#optimizeAllImages(FileTypeConversion, boolean, java.util.Collection)}
	 * with timeout set.
	 * 
	 * @throws IOException can be thrown by the 
	 *                     <code>ImageOptimizationService</code> constructor or 
	 *                     when creating a temp file used in the test.
	 * @throws IOException Thrown if there is an issue reading from the file 
	 *                     system.
	 * @throws ImageFileOptimizationException Thrown if there is an error trying
	 *                                        to optimize an image.
	 * @throws TimeoutException Thrown if optimizing an image timed out.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testOptimizeAllImagesNONEWithTimeoutSuccess() throws IOException, ImageFileOptimizationException, TimeoutException {
		
		final ImageOptimizationTestDTO[] imageOptimizationTestDTOList = {new ImageOptimizationTestDTO("csv_120.png", false, false, true),
                new ImageOptimizationTestDTO("sharing_model2.jpg", false, false, true),
                new ImageOptimizationTestDTO("loading.gif", false, false, true),
                new ImageOptimizationTestDTO("el_icon.gif", false, false, false),
                new ImageOptimizationTestDTO("safe32.png", false, false, true),
                new ImageOptimizationTestDTO("no_transparency.gif", false, false, true),
                new ImageOptimizationTestDTO("doctype_16_sprite.png", false, false, false),
                new ImageOptimizationTestDTO("addCol.gif", false, false, false),
                new ImageOptimizationTestDTO("s-arrow-bo.gif", false, false, true)};

		final int numberOfOptimizedImages = getNumberOfOptimizedImages(imageOptimizationTestDTOList);
		
		final List<File> filesToOptimize = new ArrayList<>(imageOptimizationTestDTOList.length);
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			filesToOptimize.add(imageOptimizationTestDTO.getMasterFile());
		}
		
		//Testing with NONE
		List<OptimizationResult<Object>> results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 60).optimizeAllImages(FileTypeConversion.NONE, false, filesToOptimize);
		assertThat(results, notNullValue());
		
		Map<String, OptimizationResult<Object>> treasureMap = new HashMap<>(numberOfOptimizedImages);
		for(final OptimizationResult<Object> result : results) {
			assertThat(result, notNullValue());
			treasureMap.put(result.getOriginalFile().getName(), result);
		}
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName()), imageOptimizationTestDTO, false);
		}
		assertThat(treasureMap, aMapWithSize(numberOfOptimizedImages));
		assertThat(results, hasSize(numberOfOptimizedImages));
		
		//Testing with NONE and YES WebP
		final int numberOfResultImages = numberOfOptimizedImages + getNumberOfWebPCompatibleImages(imageOptimizationTestDTOList);
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 60).optimizeAllImages(FileTypeConversion.NONE, true, filesToOptimize);
		assertThat(results, notNullValue());
		
		treasureMap = new HashMap<>(numberOfResultImages);
		for(final OptimizationResult<Object> result : results) {
			assertThat(result, notNullValue());
			if(FilenameUtils.isExtension(result.getOptimizedFile().getName(), IImageOptimizationService.WEBP_EXTENSION)) {
				treasureMap.put(result.getOriginalFile().getName() + WEBP_ID, result);
			} else {
				treasureMap.put(result.getOriginalFile().getName(), result);
			}
		}
		
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName()), imageOptimizationTestDTO, false);
		}
		assertThat(treasureMap, aMapWithSize(numberOfResultImages));
		assertThat(results, hasSize(numberOfResultImages));
		
		//WebP Check
		for(final ImageOptimizationTestDTO imageOptimizationTestDTO : imageOptimizationTestDTOList) {
			if(imageOptimizationTestDTO.isJPEG()) {
				//JPEG is not converted to WEBP
				assertThat(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), nullValue());
			} else if(imageOptimizationTestDTO.isAnimatedGif()) {
				//Animated GIF is not converted to WEBP
				assertThat(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), nullValue());
			} else {
				validateFileOptimization(treasureMap.get(imageOptimizationTestDTO.getMasterFile().getName() + WEBP_ID), imageOptimizationTestDTO, true);
			}
		}
		
		//Testing a null list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 60).optimizeAllImages(FileTypeConversion.NONE, false, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 60).optimizeAllImages(FileTypeConversion.NONE, true, (Collection<File>)null);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		//Testing an empty list of images
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 60).optimizeAllImages(FileTypeConversion.NONE, false, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
		
		results = new ImageOptimizationService<>(getTempDir(), new File(defaultBinaryAppLocation), 60).optimizeAllImages(FileTypeConversion.NONE, true, Collections.EMPTY_LIST);
		assertThat(results, notNullValue());
		assertThat(results, IsEmptyCollection.empty());
	}

	/**
	 * Test method for 
	 * {@link ImageOptimizationService#executeAdvpng(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testExecuteAdvpng() throws IOException, InterruptedException {
		
		//Test 1
		File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "owner_key_icon.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/owner_key_icon.png"), workingFile);
		long workingFileSize = workingFile.length();
		
		File optimizedFile = imageOptimizationService.executeAdvpng(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
		
		//Test 2
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "csv_120.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/csv_120.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeAdvpng(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		
		//Test 3
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "doctype_16_sprite.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/doctype_16_sprite.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeAdvpng(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
		
		//Test 4
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "sprite arrow enlarge max min shrink x blue.gif.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sprite arrow enlarge max min shrink x blue.gif.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeAdvpng(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
	}
	
	/**
	 * Test method for 
	 * {@link ImageOptimizationService#executePngquant(File, String)}
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testExecutePngquant() throws IOException, InterruptedException {
		//Test 1
		File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "owner_key_icon.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/owner_key_icon.png"), workingFile);
		long workingFileSize = workingFile.length();
		
		File optimizedFile = imageOptimizationService.executePngquant(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
		
		//Test 2
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "csv_120.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/csv_120.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executePngquant(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		
		//Test 3
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "csv_120.2png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/csv_120.2png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executePngquant(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		
		//Test 4
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "safe32.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/safe32.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executePngquant(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
		
		//Test 5
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "doctype_16_sprite.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/doctype_16_sprite.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executePngquant(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
		
		//Test 6
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "sprite arrow enlarge max min shrink x blue.gif.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sprite arrow enlarge max min shrink x blue.gif.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executePngquant(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
	}

	/**
	 * Test method for 
	 * {@link ImageOptimizationService#executePngout(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testExecutePngout() throws IOException, InterruptedException {
		
		//Test 1
		File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "owner_key_icon.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/owner_key_icon.png"), workingFile);
		long workingFileSize = workingFile.length();
		
		File optimizedFile = imageOptimizationService.executePngout(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
		
		//Test 2
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "csv_120.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/csv_120.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executePngout(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		
		//Test 3
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "sprite arrow enlarge max min shrink x blue.gif.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sprite arrow enlarge max min shrink x blue.gif.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executePngout(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
	}
	
	/**
	 * Test method for 
	 * {@link ImageOptimizationService#executePngout(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 */
	@Test
	public void testExecutePngoutException() throws IOException {

		final File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "doctype_16_sprite.png");

		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/doctype_16_sprite.png"), workingFile);
		final ImageFileOptimizationException ifoe = assertThrows(ImageFileOptimizationException.class, () -> { imageOptimizationService.executePngout(workingFile, workingFile.getCanonicalPath()); });
		assertThat(ifoe, hasProperty("message", equalTo("Error while optimizing the file \"" + workingFile.getCanonicalPath() + '"')));
	}
	
	@SuppressWarnings("boxing")
	private final void testExecuteCWebpHelper(final File fileToConvert) throws IOException, InterruptedException {
		
		final File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + fileToConvert.getName());
		
		FixedFileUtils.copyFile(fileToConvert, workingFile);
		final long workingFileSize = workingFile.length();
		
		final File optimizedFile = imageOptimizationService.executeCWebp(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		if(IImageOptimizationService.JPEG_EXTENSION.equalsIgnoreCase(FilenameUtils.getExtension(fileToConvert.getName()))) {
			assertThat(optimizedFile, aFileWithSize(greaterThan(workingFileSize)));
		} else {
			assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		}
		assertThat(optimizedFile, aFileNamed(endsWith(IImageOptimizationService.WEBP_EXTENSION)));
	}
	
	/**
	 * Test method for 
	 * {@link ImageOptimizationService#executeCWebp(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@Test
	public void testExecuteCWebp() throws IOException, InterruptedException {
		testExecuteCWebpHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/owner_key_icon.png"));
		testExecuteCWebpHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/csv_120.png"));
		testExecuteCWebpHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/doctype_16_sprite.png"));
		testExecuteCWebpHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/safe32.png"));
		testExecuteCWebpHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sharing_model2.jpg"));
		testExecuteCWebpHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sprite arrow enlarge max min shrink x blue.gif.png"));
	}
	
	@SuppressWarnings("boxing")
	private final void testExecuteGif2WebHelper(final File fileToConvert) throws IOException, InterruptedException {
		
		final File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + fileToConvert.getName());
		
		FixedFileUtils.copyFile(fileToConvert, workingFile);
		final long workingFileSize = workingFile.length();
		
		final File optimizedFile = imageOptimizationService.executeGif2Webp(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		assertThat(optimizedFile, aFileNamed(endsWith(IImageOptimizationService.WEBP_EXTENSION)));
	}
	
	/**
	 * Test method for 
	 * {@link ImageOptimizationService#executeGif2Webp(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@Test
	public void testExecuteGif2Web() throws IOException, InterruptedException {
		testExecuteGif2WebHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/el_icon.gif"));
		testExecuteGif2WebHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/loading.gif"));
		testExecuteGif2WebHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/no_transparency.gif"));
		testExecuteGif2WebHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/addCol.gif"));
		testExecuteGif2WebHelper(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/s arrow bo.gif"));
	}

	/**
	 * Test method for 
	 * {@link ImageOptimizationService#executeOptipng(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testExecuteOptipng() throws IOException, InterruptedException {
		
		File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "owner_key_icon.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/owner_key_icon.png"), workingFile);
		long workingFileSize = workingFile.length();
		
		File optimizedFile = imageOptimizationService.executeOptipng(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
		
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "csv_120.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/csv_120.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeOptipng(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "doctype_16_sprite.png");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/doctype_16_sprite.png"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeOptipng(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
	}

	/**
	 * Test for {@link ImageOptimizationService#executeJpegtran(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@Test
	public void testExecuteJpegtran() throws IOException, InterruptedException {
		
		//Test 1
		File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "sharing_model2.jpg");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sharing_model2.jpg"), workingFile);
		long workingFileSize = workingFile.length();
		
		File optimizedFile = imageOptimizationService.executeJpegtran(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
		
		//Test 2
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "sharin g model2.jpg");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sharin g model2.jpg"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeJpegtran(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(workingFileSize));
	}

	/**
	 * Test for 
	 * {@link ImageOptimizationService#executeJfifremove(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testExecuteJfifremove() throws IOException, InterruptedException {
		
		//Test 1
		File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "sharing_model2.jpg");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sharing_model2.jpg"), workingFile);
		long workingFileSize = workingFile.length();
		
		File optimizedFile = imageOptimizationService.executeJfifremove(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		
		//Test 2
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "sharin g model2.jpg");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/sharin g model2.jpg"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeJfifremove(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue());
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
	}

	/**
	 * Test for {@link ImageOptimizationService#executeGifsicle(File, String)}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 * @throws InterruptedException Can be thrown by the optimization service 
	 *                              when optimizing the files.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void testExecuteGifsicle() throws IOException, InterruptedException {
		//Test 1
		File workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "el_icon.gif");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/el_icon.gif"), workingFile);
		long workingFileSize = workingFile.length();
		
		File optimizedFile = imageOptimizationService.executeGifsicle(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue(File.class));
		assertThat(optimizedFile, anExistingFile());
		assertThat(Long.valueOf(optimizedFile.length()), equalTo(Long.valueOf(workingFileSize)));
		
		//Test 2
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "loading.gif");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/loading.gif"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeGifsicle(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue(File.class));
		assertThat(optimizedFile, anExistingFile());
		assertThat(optimizedFile, aFileWithSize(lessThan(workingFileSize)));
		
		//Test 3
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "no_transparency.gif");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/no_transparency.gif"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeGifsicle(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue(File.class));
		assertThat(optimizedFile, anExistingFile());
		assertThat(Long.valueOf(optimizedFile.length()), lessThan(Long.valueOf(workingFileSize)));
		
		//Test 4
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "addCol.gif");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/addCol.gif"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeGifsicle(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue(File.class));
		assertThat(optimizedFile, anExistingFile());
		assertThat(Long.valueOf(optimizedFile.length()), equalTo(Long.valueOf(workingFileSize)));
		
		//Test 5
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "s-arrow-bo.gif");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/s-arrow-bo.gif"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeGifsicle(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue(File.class));
		assertThat(optimizedFile, anExistingFile());
		assertThat(Long.valueOf(optimizedFile.length()), lessThan(Long.valueOf(workingFileSize)));
		
		//Test 6
		workingFile = new File(getTempDir().getCanonicalFile() + File.separator + "s arrow bo.gif");
		
		FixedFileUtils.copyFile(new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service/s arrow bo.gif"), workingFile);
		workingFileSize = workingFile.length();
		
		optimizedFile = imageOptimizationService.executeGifsicle(workingFile, workingFile.getCanonicalPath());
		assertThat(optimizedFile, notNullValue(File.class));
		assertThat(optimizedFile, anExistingFile());
		assertThat(Long.valueOf(optimizedFile.length()), lessThan(Long.valueOf(workingFileSize)));
	}

	/**
	 * Test for {@link ImageOptimizationService#getFinalResultsDirectory()}.
	 * 
	 * @throws IOException Can be thrown when interacting with various files.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetFinalResultsDirectory() throws IOException {
		final File tmpDir = getTempDir();
		
		assertThat((new ImageOptimizationService<>(tmpDir, new File(defaultBinaryAppLocation))).getFinalResultsDirectory(), equalTo(tmpDir.getCanonicalPath() + File.separator + "final"));
	}
	
	private static class ImageOptimizationTestDTO {
		
		private final File masterFile;
		private final long masterFileChecksum;
		private final boolean failedAutomatedTest;
		private final boolean fileTypeChanged;
		private final boolean isJPEG;
		private final boolean isAnimatedGif;
		private final boolean isOptimized;
		
		/**
		 * @param fileName The name of the file being tested.
		 * @param failedAutomatedTest Used to indicate if a failed automated 
		 *                            validation is expected.
		 * @param fileTypeChanged Used to indicate if a file type change is 
		 *                        expected.
		 * @param isOptimized Used to indicate if the image is expected to be 
		 *                    optimized.
		 * @throws IOException Thrown when calculating the masterFileChecksum
		 */
		ImageOptimizationTestDTO(final String fileName, final boolean failedAutomatedTest, final boolean fileTypeChanged, final boolean isOptimized) throws IOException {
			masterFile = new File("./src/test/java/com/salesforce/perfeng/uiperf/imageoptimization/service" + File.separator + fileName);
			assertThat("Issue with test setup.  Looks like the required file for the test is not present.", masterFile, FileMatchers.anExistingFile());
			masterFileChecksum = FileUtils.checksumCRC32(masterFile);
			this.failedAutomatedTest = failedAutomatedTest;
			this.fileTypeChanged = fileTypeChanged;
			this.isJPEG = (IImageOptimizationService.JPEG_EXTENSION.equalsIgnoreCase(FilenameUtils.getExtension(fileName)));
			this.isAnimatedGif = ImageUtils.isAminatedGif(masterFile);
			this.isOptimized = isOptimized;
		}
		
		public File getMasterFile() {
			return masterFile;
		}
		public long getMasterFileChecksum() {
			return masterFileChecksum;
		}
		public boolean isFailedAutomatedTest() {
			return failedAutomatedTest;
		}
		public boolean isFileTypeChanged() {
			return fileTypeChanged;
		}
		public boolean isJPEG() {
			return isJPEG;
		}
		public boolean isAnimatedGif() {
			return isAnimatedGif;
		}
		public boolean isOptimized() {
			return isOptimized;
		}
	}
}