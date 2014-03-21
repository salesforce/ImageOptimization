package com.salesforce.perfeng.uiperf.imageoptimization.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.salesforce.perfeng.uiperf.ThirdPartyBinaryNotFoundException;
import com.salesforce.perfeng.uiperf.imageoptimization.dto.OptimizationResult;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.FixedFileUtils;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageFileOptimizationException;
import com.salesforce.perfeng.uiperf.imageoptimization.utils.ImageUtils;

/**
 * Service used to perform the optimization of images.
 * 
 * @author eperret (Eric Perret)
 * @since 186.internal
 * @param <C> Contains the changeList information.
 */
public class ImageOptimizationService<C> implements IImageOptimizationService<C> {

	/**
	 * Log4j2 logger for this class and inner classes.
	 */
	final static Logger logger = LoggerFactory.getLogger(ImageOptimizationService.class);
	
	
	/**
	 * Internal error message used when an error occurred while optimizing a GIF
	 * image.
	 */
	static final String GIF_ERROR_MESSAGE;
	/**Internal error message used when an error occurred while optimizing a
	 * JPEG image.
	 * 
	 */
	static final String JPEG_ERROR_MESSAGE;
	/**
	 * Internal error message used when an error occurred while optimizing a PNG
	 * image.
	 */
	static final String PNG_ERROR_MESSAGE;
	/**
	 * Internal error message used when an error occurred while converting an 
	 * image to WEBP.
	 */
	static final String WEBP_ERROR_MESSAGE;
	
	static {
		final String common = "Error %s %s. This image will be skipped. Usually this is caused by the original image being in an unsupported format or corrupted (or not an image). Moving on with the rest of the optimizations.";
		GIF_ERROR_MESSAGE  = String.format(common, "optimizing", IImageOptimizationService.GIF_EXTENSION.toUpperCase());
		JPEG_ERROR_MESSAGE = String.format(common, "optimizing", IImageOptimizationService.JPEG_EXTENSION.toUpperCase());
		PNG_ERROR_MESSAGE  = String.format(common, "optimizing", IImageOptimizationService.PNG_EXTENSION.toUpperCase());
		WEBP_ERROR_MESSAGE = String.format(common, "converting to", IImageOptimizationService.WEBP_EXTENSION.toUpperCase());
	}
	
	/**
	 * Name of the "cwebp" binary application used to convert a 
	 * non-{@value IImageOptimizationService#GIF_MIME_TYPE} file to a 
	 * {@value IImageOptimizationService#WEBP_MIME_TYPE} file.
	 */
	protected static final String CWEBP_BINARY     = "cwebp";
	/**
	 * Name of the {@value #GIF2WEBP_BINARY} binary application used to convert 
	 * a {@value IImageOptimizationService#GIF_MIME_TYPE} file to a 
	 * {@value IImageOptimizationService#WEBP_MIME_TYPE} file.
	 */
	protected static final String GIF2WEBP_BINARY  = "gif2webp";
	/**
	 * Name of the {@value #GIFSICLE_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#GIF_MIME_TYPE} file.
	 */
	protected static final String GIFSICLE_BINARY  = "gifsicle";
	/**
	 * Name of the {@value #JPEGTRAN_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#JPEG_MIME_TYPE} file. On linux this 
	 * app requires libjpeg62 to be installed. Run 
	 * "sudo apt-get install libjpeg62".
	 */
	protected static final String JPEGTRAN_BINARY  = "jpegtran";
	/**
	 * Name of the {@value #JFIFREMOVE_BINARY} binary application used to 
	 * optimize a {@value IImageOptimizationService#JPEG_MIME_TYPE} file.
	 */
	protected static final String JFIFREMOVE_BINARY = "jfifremove";
	/**
	 * Name of the {@value #ADVPNG_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#PNG_MIME_TYPE} file.
	 */
	protected static final String ADVPNG_BINARY    = "advpng";
	/**
	 * Name of the {@value #OPTIPNG_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#PNG_MIME_TYPE} file.
	 */
	protected static final String OPTIPNG_BINARY   = "optipng";
	/**
	 * Name of the {@value #PNGOUT_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#PNG_MIME_TYPE} file.
	 */
	protected static final String PNGOUT_BINARY    = "pngout";
	
	/**
	 * Path of the "cwebp" binary application used to convert a 
	 * non-{@value IImageOptimizationService#GIF_MIME_TYPE} file to a 
	 * {@value IImageOptimizationService#WEBP_MIME_TYPE} file.
	 */
	protected final String cwebpBinaryPath;
	/**
	 * Path of the {@value #GIF2WEBP_BINARY} binary application used to convert 
	 * a {@value IImageOptimizationService#GIF_MIME_TYPE} file to a 
	 * {@value IImageOptimizationService#WEBP_MIME_TYPE} file.
	 */
	protected final String gif2webpBinaryPath;
	/**
	 * Path of the {@value #GIFSICLE_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#GIF_MIME_TYPE} file.
	 */
	protected final String gifsicleBinaryPath;
	/**
	 * Path of the {@value #JPEGTRAN_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#JPEG_MIME_TYPE} file.
	 */
	protected final String jpegtranBinaryPath;
	/**
	 * Path of the {@value #JFIFREMOVE_BINARY} binary application used to 
	 * optimize a {@value IImageOptimizationService#JPEG_MIME_TYPE} file.
	 */
	protected final String jfifremoveBinaryPath;
	/**
	 * Path of the {@value #ADVPNG_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#PNG_MIME_TYPE} file.
	 */
	protected final String advpngBinaryPath;
	/**
	 * Path of the {@value #OPTIPNG_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#PNG_MIME_TYPE} file.
	 */
	protected final String optipngBinaryPath;
	/**
	 * Path of the {@value #PNGOUT_BINARY} binary application used to optimize 
	 * a {@value IImageOptimizationService#PNG_MIME_TYPE} file.
	 */
	protected final String pngoutBinaryPath;
	
	private final int MAX_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

	private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_THREADS, new ThreadFactory() {
		/**
		 * Makes the thread daemon threads so they can be killed automatically 
		 * when the parent thread is done running
		 * 
		 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		@Override
		public Thread newThread(final Runnable runnable) {
			final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setDaemon(true);
			return thread;
		}
	});

	private final File tmpWorkingDirectory;
	private final String finalWorkingDirectoryPath;

	/**
	 * Constructor that sets the working directories and root directories.
	 * 
	 * @param tmpWorkingDirectory This is the temp directory where all of the 
	 *                            images will be optimized from and stored 
	 *                            before they are checked back into P4.
	 * @param binaryDirectory The location the binary image compression programs
	 *                        are located.
	 * @throws IOException Thrown when interacting with the tmpWorkingDirectory
	 */
	public ImageOptimizationService(final File tmpWorkingDirectory, final File binaryDirectory) throws IOException {
		if(tmpWorkingDirectory == null) {
			throw new IllegalArgumentException("The passed in tmpWorkingDirectory needs to exist.");
		}else if(binaryDirectory == null) {
			throw new IllegalArgumentException("The passed in binaryDirectory needs to exist.");
		}  else if(!tmpWorkingDirectory.isDirectory()) {
			throw new IllegalArgumentException("The passed in tmpWorkingDirectory, \"" + tmpWorkingDirectory.getCanonicalPath() + "\", needs to be a directory.");
		} else if(!binaryDirectory.isDirectory()) {
			throw new IllegalArgumentException("The passed in binaryDirectory , \"" + binaryDirectory.getCanonicalPath() + "\", needs to exist and be a directory.");
		}
		this.tmpWorkingDirectory = tmpWorkingDirectory.getCanonicalFile();

		finalWorkingDirectoryPath = new StringBuilder(tmpWorkingDirectory.getCanonicalPath()).append(File.separatorChar).append("final").toString();
		
		final String binaryDirectoryPath = binaryDirectory.getAbsolutePath() + File.separator;
		
		cwebpBinaryPath      = binaryDirectoryPath + "cwebp";
		gif2webpBinaryPath    = binaryDirectoryPath + "gif2webp";
		gifsicleBinaryPath   = binaryDirectoryPath + "gifsicle";
		jpegtranBinaryPath   = binaryDirectoryPath + "jpegtran";
		// Needs to be escaped because it is passed as an argument to the bash 
		// command.
		jfifremoveBinaryPath = escapeUnixPath(binaryDirectoryPath + "jfifremove");
		advpngBinaryPath     = binaryDirectoryPath + "advpng";
		optipngBinaryPath    = binaryDirectoryPath + "optipng";
		pngoutBinaryPath     = binaryDirectoryPath + "pngout";
	}
	
	private static String escapeUnixPath(final String workingFilePath) {
		return workingFilePath.replaceAll(" ", "\\\\ ");
	}

	/**
	 * Used to create a new instance of this class.
	 * 
	 * @param pathToBinaryProgramsForImageOptimizationDirectory This is the 
	 *                                                          location where 
	 *                                                          the image 
	 *                                                          optimization 
	 *                                                          binary 
	 *                                                          applications are
	 *                                                          location. It can
	 *                                                          be relative or 
	 *                                                          absolute.
	 * @param <C> Holds the changelist information.
	 * @return An instance of this class.
	 * @throws IOException Thrown when creating the tmp working directory
	 */
	public final static <C> ImageOptimizationService<C> createInstance(final String pathToBinaryProgramsForImageOptimizationDirectory) throws IOException {
		if(logger.isDebugEnabled()) {
			logger.debug("Current local directory is: {}", new File(".").getCanonicalPath());
		}
		
		final File tmpDir = File.createTempFile(ImageOptimizationService.class.getName(), "");
		tmpDir.delete();
		tmpDir.mkdir();
		return new ImageOptimizationService<>(tmpDir, new File(pathToBinaryProgramsForImageOptimizationDirectory).getCanonicalFile());
	}

	/**
	 * Copies the image from the working temp directory to the correct directory
	 * under min where all of the optimized images will be stored.
	 * 
	 * @param masterFile The original image.
	 * @param workingFile The optimized file.
	 * @param fileTypeChanged <code>true</code> if the file changed extensions /
	 *                        mime types.
	 * @return The {@link File} pointing to the final location of the optimized 
	 * 		   file. It can return <code>null</code> if creating the optimized 
	 *         file would overwrite an existing file.
	 * @throws IOException Can be thrown when copying the file.
	 */
	File copyFileToMinifiedDirectory(final File masterFile, final File workingFile, final boolean fileTypeChanged) throws IOException {

		final StringBuilder sb = new StringBuilder(finalWorkingDirectoryPath);

		if(fileTypeChanged) {
			final StringBuilder newFilePath = new StringBuilder(FilenameUtils.removeExtension(masterFile.getAbsolutePath())).append('.').append(FilenameUtils.getExtension(workingFile.getName()));
			if(new File(newFilePath.toString()).exists()) {
				if(logger.isInfoEnabled()) {
					logger.info("Returning null because file extension changed and the new file already exists.\n\tmasterFile: {}\n\tworkingFile: {}\n\tfileTypeChanged: {}", masterFile.getCanonicalPath(), workingFile.getCanonicalPath(), Boolean.valueOf(fileTypeChanged));
				}
				return null;
			}
			sb.append(newFilePath);
		} else {
			sb.append(masterFile.getAbsolutePath());
		}

		final File minifiedFile = new File(sb.toString());
		if(minifiedFile.exists()) {
			if(logger.isWarnEnabled()) {
				logger.warn("Returning null, file already exists at {}\n\tmasterFile: {}\n\tworkingFile: {}\n\tfileTypeChanged: {}", minifiedFile.getCanonicalPath(), masterFile.getCanonicalPath(), workingFile.getCanonicalPath(), Boolean.valueOf(fileTypeChanged));
			}
			return null;
		}

		FixedFileUtils.copyFile(workingFile, minifiedFile);
		return minifiedFile;
	}

	/**
	 * Submits the {@link Callable} that will optimize the passed in image.
	 * 
	 * @param file The file to optimize.
	 * @param conversionType If and how to handle converting images from one 
	 *                       type to another.
	 * @param tmpImageWorkingDirectory the working directory for optimizing the 
	 *                                 files.
	 * @return the number of tasks submitted.
	 * @throws ImageFileOptimizationException Thrown if an error occurs.
	 */
	private final int submitExecuteOptimization(final CompletionService<OptimizationResult<C>> completionService, final File file, final StringBuilder tmpImageWorkingDirectory, final FileTypeConversion conversionType, final boolean includeWebPConversion) throws ImageFileOptimizationException {
		try {
			final String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
			if(PNG_EXTENSION.equals(ext)) {
				completionService.submit(new ExecutePngOptimization(file.getCanonicalFile(), new File(new StringBuilder(tmpImageWorkingDirectory).append(file.getCanonicalPath()).toString()), conversionType));
				if(includeWebPConversion) {
					completionService.submit(new ExecuteWebpConversion(file.getCanonicalFile(), new File(new StringBuilder(tmpImageWorkingDirectory).append(IImageOptimizationService.WEBP_EXTENSION).append(file.getCanonicalPath()).toString()), false));
					return 2;
				}
			} else if(GIF_EXTENSION.equals(ext)) {
				completionService.submit(new ExecuteGifOptimization(file.getCanonicalFile(), new File(new StringBuilder(tmpImageWorkingDirectory).append(file.getCanonicalPath()).toString()), conversionType));
				if(includeWebPConversion) {
					completionService.submit(new ExecuteWebpConversion(file.getCanonicalFile(), new File(new StringBuilder(tmpImageWorkingDirectory).append(IImageOptimizationService.WEBP_EXTENSION).append(file.getCanonicalPath()).toString()), true));
					return 2;
				}
			} else if(JPEG_EXTENSION.equals(ext)) {
				completionService.submit(new ExecuteJpegOptimization(file.getCanonicalFile(), new File(new StringBuilder(tmpImageWorkingDirectory).append(file.getCanonicalPath()).toString()), conversionType));
			} else {
				throw new IllegalArgumentException("The passed in file has an unsupported file extension.");
			}
			return 1;
		} catch (final Exception e) {
			throw ImageFileOptimizationException.getInstance(file, e);
		}
	}

	private final static <C> List<OptimizationResult<C>> optimizeGroupOfImages(final CompletionService<OptimizationResult<C>> completionService, final long numberOfThreads) {

		OptimizationResult<C> optimizationResult;

		final List<OptimizationResult<C>> masterListOfOptimizedFiles = new ArrayList<>();

		for(int i = 0; i < numberOfThreads; i++) {
			try {
				optimizationResult = completionService.take().get();
				if(optimizationResult != null) {
					logger.info(optimizationResult.toString());
					masterListOfOptimizedFiles.add(optimizationResult);
				}
			} catch (final ExecutionException ee) {
				throw new RuntimeException(ee);
			} catch (final InterruptedException ie) {
				throw new RuntimeException(ie);
			}
		}
		return masterListOfOptimizedFiles;
	}

	private final static void handleOptimizationFailure(final Process ps, final String binaryApplicationName, final File originalFile) throws ThirdPartyBinaryNotFoundException, ImageFileOptimizationException {

		try(final StringWriter writer = new StringWriter();
			final InputStream is      = ps.getInputStream()) {
			try {
				IOUtils.copy(is, writer);
				final StringBuilder errorMessage = new StringBuilder("Optimization failed with edit code: ").append(ps.exitValue()).append(". ").append(writer);
				if(ps.exitValue() == 127 /* command not found */) {
					throw new ThirdPartyBinaryNotFoundException(binaryApplicationName, "Most likely this is due to required libraries not being installed on the OS. On Ubuntu run \"sudo apt-get install libjpeg62\".", new RuntimeException(errorMessage.toString()));
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
	 * 
	 * Optimizes all of the passed in images. This process is multi-threaded so
	 * that the number of threads is equal to the number of CPUs.
	 * 
	 * @param conversionType If and how to handle converting images from one 
	 *                       type to another.
	 * @param includeWebPConversion If <code>true</code> then the a WebP version 
	 *                              of the image will also be generated (if it 
	 *                              is smaller).
	 * @param files The images to optimize
	 * @return The results from the optimization. All items in the {@link List}
	 *         are considered optimized, not <code>null</code>, and will exclude
	 *         images that could not be optimized to a smaller size.
	 * @throws ImageFileOptimizationException If there are any issues optimizing
	 *                                        an image.
	 * @see #optimizeAllImages(com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.FileTypeConversion, boolean, File...)
	 * @see com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService#optimizeAllImages(FileTypeConversion, boolean, Collection)
	 */
	@Override
	public List<OptimizationResult<C>> optimizeAllImages(final FileTypeConversion conversionType, final boolean includeWebPConversion, final Collection<File> files) throws ImageFileOptimizationException {
		if((files == null) || files.isEmpty()) {
			return Collections.emptyList();
		}

		final CompletionService<OptimizationResult<C>> completionService = new ExecutorCompletionService<>(executorService);

		int i = 0;
		int numberOfSubmittedImages = 0;
		final Date start = new Date();
		final long time = System.nanoTime();
		for(final File file : files) {
			numberOfSubmittedImages += submitExecuteOptimization(completionService, file, new StringBuilder(tmpWorkingDirectory.getAbsolutePath()).append(File.separatorChar).append("scratch").append(time).append(i++), conversionType, includeWebPConversion);
		}
		final List<OptimizationResult<C>> optimizedFiles = optimizeGroupOfImages(completionService, numberOfSubmittedImages);
		logger.info("Image optimization elapsed time: " + (new Date().getTime() - start.getTime()));

		return optimizedFiles;
	}

	/**
	 * Optimizes all of the passed in images. This process is multi-threaded so
	 * that the number of threads is equal to the number of CPUs.
	 * 
	 * @param conversionType If and how to handle converting images from one 
	 *                       type to another.
	 * @param includeWebPConversion If <code>true</code> then the a WebP version 
	 *                              of the image will also be generated (if it 
	 *                              is smaller).
	 * @param files The images to optimize
	 * @return The results from the optimization. All items in the {@link List}
	 *         are considered optimized, not <code>null</code>, and will exclude
	 *         images that could not be optimized to a smaller size.
	 * @throws ImageFileOptimizationException If there are any issues optimizing
	 *                                        an image.
	 * @see #optimizeAllImages(com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.FileTypeConversion, boolean, Collection)
	 * @see com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService#optimizeAllImages(com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService.FileTypeConversion, boolean, java.io.File[])
	 */
	@Override
	public List<OptimizationResult<C>> optimizeAllImages(final FileTypeConversion conversionType, final boolean includeWebPConversion, final File... files) throws ImageFileOptimizationException {
		return optimizeAllImages(conversionType, includeWebPConversion, new HashSet<>(Arrays.asList(files)));
	}

	/**
	 * Executes the binary {@value #ADVPNG_BINARY} to optimize the input file.
	 * 
	 * @param workingFile The file to optimize
	 * @param workingFilePath The path to the file to optimize
	 * @return the optimized file
	 * @throws InterruptedException If the optimization was interrupted.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           {@value #ADVPNG_BINARY}
	 *                                           application does not exist.
	 */
	final File executeAdvpng(final File workingFile, final String workingFilePath) throws InterruptedException, ThirdPartyBinaryNotFoundException {

		final Process ps;
		try {
			final ProcessBuilder pb = new ProcessBuilder(advpngBinaryPath, "-z", "-4", workingFilePath);
			pb.redirectErrorStream(true);
			ps = pb.start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(ADVPNG_BINARY, ioe);
		}
		ps.waitFor();
		if(ps.exitValue() != 0) {
			handleOptimizationFailure(ps, ADVPNG_BINARY, workingFile);
		}
		return workingFile;
	}

	/**
	 * Executes the binary {@value #PNGOUT_BINARY} to optimize the input file.
	 * 
	 * @param workingFile The file to optimize
	 * @param workingFilePath The path to the file to optimize
	 * @return the optimized file
	 * @throws InterruptedException If the optimization was interrupted.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           {@value #PNGOUT_BINARY}
	 *                                           application does not exist.
	 */
	final File executePngout(final File workingFile, final String workingFilePath) throws InterruptedException, ThirdPartyBinaryNotFoundException {

		final Process ps;
		try {
			final ProcessBuilder pb = new ProcessBuilder(pngoutBinaryPath, workingFilePath, workingFilePath, "-y");
			pb.redirectErrorStream(true);
			ps = pb.start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(PNGOUT_BINARY, ioe);
		}

		ps.waitFor();
		if(ps.exitValue() != 0 && ps.exitValue() != 2) {
			handleOptimizationFailure(ps, PNGOUT_BINARY, workingFile);
		} else {
			final File newFile = new File(workingFilePath + "." + PNG_EXTENSION);
			if(newFile.exists()) {
				workingFile.delete();
				if(!newFile.renameTo(workingFile)) {
					logger.warn("Optimization failed to copy file. Moving on with the test.", ImageFileOptimizationException.getInstance(workingFile, "Optimization failed to copy file. Moving on with the test."));
				}
			}
		}
		return workingFile;
	}

	/**
	 * Executes the binary {@value #OPTIPNG_BINARY} to optimize the input file.
	 * 
	 * @param workingFile The file to optimize
	 * @param workingFilePath The path to the file to optimize
	 * @return the optimized file
	 * @throws InterruptedException If the optimization was interrupted.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           {@value #OPTIPNG_BINARY}
	 *                                           application does not exist.
	 */
	final File executeOptipng(final File workingFile, final String workingFilePath) throws InterruptedException, ThirdPartyBinaryNotFoundException {

		final Process ps;
		try {
			final ProcessBuilder pb = new ProcessBuilder(optipngBinaryPath, "-zc9", "-zm8-9", "-zs0-3", "-f0-5", "-zw", "32k", workingFilePath);
			pb.redirectErrorStream(true);
			ps = pb.start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(OPTIPNG_BINARY, ioe);
		}
		if(ps.waitFor() != 0) {
			handleOptimizationFailure(ps, OPTIPNG_BINARY, workingFile);
		}

		return workingFile;
	}

	/**
	 * Executes the binary {@value #JPEGTRAN_BINARY} to optimize the input file.
	 * 
	 * @param workingFile The file to optimize
	 * @param workingFilePath The path to the file to optimize
	 * @return the optimized file
	 * @throws InterruptedException If the optimization was interrupted.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           {@value #JPEGTRAN_BINARY} 
	 *                                           application does not exist.
	 */
	final File executeJpegtran(final File workingFile, final String workingFilePath) throws InterruptedException, ThirdPartyBinaryNotFoundException {

		final Process ps;
		try {
			final ProcessBuilder pb = new ProcessBuilder(jpegtranBinaryPath, "-copy", "none", "-optimize", "-outfile", workingFilePath + ".tmp", workingFilePath);
			pb.redirectErrorStream(true);
			ps = pb.start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(JPEGTRAN_BINARY, ioe);
		}

		if(ps.waitFor() == 0) {
			final File tmpFile = new File(workingFilePath + ".tmp");
			if(tmpFile.length() < workingFile.length()) {
				return tmpFile;
			}
		} else {
			handleOptimizationFailure(ps, JPEGTRAN_BINARY, workingFile);
		}

		return workingFile;
	}

	/**
	 * Executes the binary {@value #JFIFREMOVE_BINARY}" to optimize the input 
	 * file.
	 * 
	 * @param workingFile The file to optimize
	 * @param workingFilePath The path to the file to optimize
	 * @return the optimized file
	 * @throws InterruptedException If the optimization was interrupted.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           "jfifremove" application does 
	 *                                           not exist.
	 */
	final File executeJfifremove(final File workingFile, final String workingFilePath) throws InterruptedException, ThirdPartyBinaryNotFoundException {

		final Process ps;
		try {
			final String escapedWorkingFilePath = escapeUnixPath(workingFilePath);
			//Can't redirect the Error stream because it is already redirecting 
			//the output.
			ps = new ProcessBuilder("bash", "-c", new StringBuilder(jfifremoveBinaryPath).append(" < ").append(escapedWorkingFilePath).append(" > ").append(escapedWorkingFilePath).append(".tmp2").toString()).start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(JFIFREMOVE_BINARY, ioe);
		}

		if(ps.waitFor() != 0) {
			handleOptimizationFailure(ps, JFIFREMOVE_BINARY, workingFile);
		}

		return new File(workingFilePath + ".tmp2");
	}

	/**
	 * Executes the binary {@value #GIFSICLE_BINARY} to optimize the input file.
	 * 
	 * @param workingFile The file to optimize
	 * @param workingFilePath The path to the file to optimize
	 * @return the optimized file
	 * @throws InterruptedException If the optimization was interrupted.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           {@value #GIFSICLE_BINARY} 
	 *                                           application does not exist.
	 */
	final File executeGifsicle(final File workingFile, final String workingFilePath) throws InterruptedException, ThirdPartyBinaryNotFoundException {

		final Process ps;
		try {
			final ProcessBuilder pb = new ProcessBuilder(gifsicleBinaryPath, "-O3", workingFilePath, "-o", workingFilePath + ".tmp");
			pb.redirectErrorStream(true);
			ps = pb.start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(GIFSICLE_BINARY, ioe);
		}

		if(ps.waitFor() == 1) {
			final File tmpFile = new File(workingFilePath + ".tmp");
			if(tmpFile.exists()) {
				return tmpFile;
			}
			handleOptimizationFailure(ps, GIFSICLE_BINARY, workingFile);
		} else if(ps.exitValue() != 0) {
			handleOptimizationFailure(ps, GIFSICLE_BINARY, workingFile);
		}

		return new File(workingFilePath + ".tmp");
	}

	/**
	 * Executes the binary {@value #CWEBP_BINARY} to convert the input file to 
	 * a smaller file. The resulting image is only supported by Chrome and Opera
	 * 
	 * @param workingFile The file to convert
	 * @param workingFilePath The path to the file to convert
	 * @return The converted file
	 * @throws InterruptedException If the optimization was interrupted.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           {@value #CWEBP_BINARY} 
	 *                                           application does not exist.
	 */
	final File executeCWebp(final File workingFile, final String workingFilePath) throws InterruptedException, ThirdPartyBinaryNotFoundException {
		final String webpFilePath = FilenameUtils.removeExtension(workingFilePath) + "." + WEBP_EXTENSION;

		final Process ps;
		try {
			final ProcessBuilder pb = new ProcessBuilder(cwebpBinaryPath, workingFilePath, "-lossless", "-m", "6", "-o", webpFilePath);
			pb.redirectErrorStream(true);
			ps = pb.start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(CWEBP_BINARY, ioe);
		}

		File webpFile = null;
		if(ps.waitFor() == 0) {
			webpFile = new File(webpFilePath);
			if(webpFile.exists()) {
				return webpFile;
			}
			handleOptimizationFailure(ps, CWEBP_BINARY, workingFile);
		} else {
			handleOptimizationFailure(ps, CWEBP_BINARY, workingFile);
		}

		return webpFile;
	}

	/**
	 * Executes the binary {@value #GIF2WEBP_BINARY} to convert the input file 
	 * to a smaller file. The resulting image is only supported by Chrome and 
	 * Opera
	 * 
	 * @param workingFile The file to convert
	 * @param workingFilePath The path to the file to convert
	 * @return The converted file
	 * @throws InterruptedException If the optimization was interrupted.
	 * @throws ThirdPartyBinaryNotFoundException Thrown if the 
	 *                                           {@value #GIF2WEBP_BINARY} 
	 *                                           application does not exist.
	 */
	final File executeGif2Webp(final File workingFile, final String workingFilePath) throws InterruptedException, ThirdPartyBinaryNotFoundException {
		final String webpFilePath = FilenameUtils.removeExtension(workingFilePath) + "." + WEBP_EXTENSION;

		final Process ps;
		try {
			final ProcessBuilder pb = new ProcessBuilder(gif2webpBinaryPath, workingFilePath, "-m", "6", "-o", webpFilePath);
			pb.redirectErrorStream(true);
			ps = pb.start();
		} catch(final IOException ioe) {
			throw new ThirdPartyBinaryNotFoundException(GIF2WEBP_BINARY, ioe);
		}

		File webpFile = null;
		if(ps.waitFor() == 0) {
			webpFile = new File(webpFilePath);
			if(webpFile.exists()) {
				return webpFile;
			}
			handleOptimizationFailure(ps, GIF2WEBP_BINARY, workingFile);
		} else {
			handleOptimizationFailure(ps, GIF2WEBP_BINARY, workingFile);
		}

		return webpFile;
	}

	private final class ExecutePngOptimization implements Callable<OptimizationResult<C>> {

		private final File masterFile;
		private final File workingFile;
		//TODO Support type conversions.
		private final FileTypeConversion conversionType;

		/**
		 * @param masterFile The original image.
		 * @param workingFile The tmp file to optimize.
		 * @param conversionType If and how to handle converting images from one 
		 *                       type to another.
		 */
		public ExecutePngOptimization(final File masterFile, final File workingFile, final FileTypeConversion conversionType) {
			this.workingFile = workingFile;
			this.masterFile = masterFile;
			this.conversionType = conversionType;
		}

		/**
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public OptimizationResult<C> call() {

			File optimizedFile = null;
			try {
				FixedFileUtils.copyFile(masterFile, workingFile);

				optimizedFile = executeOptimization();

				final long masterFileSize = masterFile.length();

				if(optimizedFile.length() < masterFileSize) {

					final File finalFile = copyFileToMinifiedDirectory(masterFile, optimizedFile, false);
					if(finalFile == null) {
						return null;
					}
					return new OptimizationResult<>(finalFile, finalFile.length(), masterFile, masterFileSize, false, !ImageUtils.visuallyCompare(optimizedFile, masterFile), false);
				}
			} catch(final ThirdPartyBinaryNotFoundException tpbnfe) {
				throw tpbnfe;
			} catch (final Exception e) {
				logger.warn(PNG_ERROR_MESSAGE, new ImageFileOptimizationException(masterFile.getPath(), e));
			} finally {
				if(optimizedFile != null) {
					try {
						FileUtils.forceDelete(optimizedFile.getParentFile());
					} catch (final IOException ioe) {
						logger.warn("Error deleting temp file.", ioe);
					}
				}
			}
			return null;
		}

		/**
		 * Executes the PNGOut, OptiPNG, and AdvPNG optimization programs on the
		 * working file passed into the constructor.
		 * 
		 * @return The optimized file.
		 * @throws IOException If there was an issue reading / writing to the 
		 *                     file system
		 * @throws InterruptedException If the optimization was interrupted.
		 */
		public File executeOptimization() throws IOException, InterruptedException {
			final String path = workingFile.getCanonicalPath();
			// FIXME Handle the ImageFileOptimizationException in one of the optimizations so it does not impact the other optimizations.
			return executeOptipng(executePngout(executeAdvpng(executeOptipng(executePngout(executeAdvpng(workingFile, path), path), path), path), path), path);
		}
	}

	private final class ExecuteJpegOptimization implements Callable<OptimizationResult<C>> {

		private final File masterFile;
		private final File workingFile;
		//TODO Support type conversions
		private final FileTypeConversion conversionType;

		/**
		 * @param masterFile The original image
		 * @param workingFile The copy of the file to optimize
		 * @param conversionType If and how to handle converting images from one 
		 *                       type to another.
		 */
		public ExecuteJpegOptimization(final File masterFile, final File workingFile, final FileTypeConversion conversionType) {
			this.workingFile = workingFile;
			this.masterFile = masterFile;
			this.conversionType = conversionType;
		}

		/**
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public OptimizationResult<C> call() {

			File optimizedFile = null;
			try {
				FixedFileUtils.copyFile(masterFile, workingFile);

				optimizedFile = executeJpegtran(workingFile, workingFile.getCanonicalPath());
				optimizedFile = executeJfifremove(optimizedFile, optimizedFile.getCanonicalPath());

				final long masterFileSize = masterFile.length();

				if(optimizedFile.length() < masterFileSize) {
					final File finalFile = copyFileToMinifiedDirectory(masterFile, optimizedFile, false);
					if(finalFile == null) {
						return null;
					}
					
					return new OptimizationResult<>(finalFile, finalFile.length(), masterFile, masterFileSize, false, !ImageUtils.visuallyCompare(finalFile, masterFile), false);
				}
			} catch(final ThirdPartyBinaryNotFoundException tpbnfe) {
				throw tpbnfe;
			} catch (final Exception e) {
				logger.warn(JPEG_ERROR_MESSAGE, new ImageFileOptimizationException(masterFile.getPath(), e));
			} finally {
				if(optimizedFile != null) {
					try {
						FileUtils.forceDelete(optimizedFile.getParentFile());
					} catch (final IOException ioe) {
						logger.warn("Error deleting temp file.", ioe);
					}
				}
			}
			return null;
		}
	}

	private final class ExecuteWebpConversion implements Callable<OptimizationResult<C>> {

		private final File masterFile;
		private final File workingFile;
		private final boolean isGif;

		/**
		 * @param masterFile The original image
		 * @param workingFile The copy of the file to optimize
		 * @param isGif If <code>true</code> then use 
		 *              {@link ImageOptimizationService#executeGif2Webp(File, String)}
		 *              to convert the file to WebP. If <code>false</code> then 
		 *              use
		 *              {@link ImageOptimizationService#executeCWebp(File, String)}
		 *              to convert the image to WebP.
		 */
		public ExecuteWebpConversion(final File masterFile, final File workingFile, final boolean isGif) {
			this.workingFile = workingFile;
			this.masterFile = masterFile;
			this.isGif = isGif;
		}

		/**
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public OptimizationResult<C> call() {

			File optimizedFile = null;
			try {
				FixedFileUtils.copyFile(masterFile, workingFile);

				if(!isGif || !ImageUtils.isAminatedGif(workingFile)) {

					optimizedFile = isGif ? executeGif2Webp(workingFile, workingFile.getCanonicalPath()) : executeCWebp(workingFile, workingFile.getCanonicalPath());

					final long masterFileSize = masterFile.length();

					if(optimizedFile.length() < masterFileSize) {
						final File finalFile = copyFileToMinifiedDirectory(masterFile, optimizedFile, true);
						if(finalFile == null) {
							return null;
						}
						return new OptimizationResult<>(finalFile, finalFile.length(), masterFile, masterFileSize, true, false, true);
					}
				}
			} catch(final ThirdPartyBinaryNotFoundException tpbnfe) {
				throw tpbnfe;
			} catch (final Exception e) {
				logger.warn(WEBP_ERROR_MESSAGE, new ImageFileOptimizationException(masterFile.getPath(), e));
			} finally {
				if(optimizedFile != null) {
					try {
						FileUtils.forceDelete(optimizedFile.getParentFile());
					} catch (final IOException ioe) {
						logger.warn("Error deleting temp file.", ioe);
					}
				}
			}
			return null;
		}
	}

	private final class ExecuteGifOptimization implements Callable<OptimizationResult<C>> {

		private final File masterFile;
		private final File workingFile;
		private final FileTypeConversion conversionType;

		/**
		 * @param masterFile The original file
		 * @param workingFile The working copy of the file which will be 
		 *                    optimized
		 * @param conversionType If and how to handle converting images from one 
		 *                       type to another.
		 */
		public ExecuteGifOptimization(final File masterFile, final File workingFile, final FileTypeConversion conversionType) {
			this.workingFile = workingFile;
			this.masterFile = masterFile;
			this.conversionType = conversionType;
		}

		private boolean isFileTypeConversionEnabled(final File optimizedFile) {
			if(FileTypeConversion.isEnabled(conversionType) && !ImageUtils.isAminatedGif(optimizedFile)) {
				if((conversionType == FileTypeConversion.IE6SAFE) && !ImageUtils.containsAlphaTransparency(optimizedFile)) {
					return true;
				}
				return (conversionType == FileTypeConversion.ALL);
			}
			return false;
		}

		/**
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public OptimizationResult<C> call() {

			File optimizedFile = null;
			try {
				boolean fileTypeChanged = false;

				FixedFileUtils.copyFile(masterFile, workingFile);

				optimizedFile = executeGifsicle(workingFile, workingFile.getCanonicalPath());

				boolean answer;
				try {
					answer = isFileTypeConversionEnabled(optimizedFile);
				} catch(final Exception e) {
					logger.debug("The image must be corrupted. Ignoring the error.", e);
					answer = false;
				}

				if(answer) {

					final File workingFilePng = new File(FilenameUtils.removeExtension(workingFile.getCanonicalPath()) + "." + PNG_EXTENSION);
					final File workingFilePng2 = new File(FilenameUtils.removeExtension(workingFile.getCanonicalPath()) + ".2" + PNG_EXTENSION);

					File optimizedFilePng = null;
					try {
						//First try optimizing the PNG version of the optimized GIF
						ImageIO.write(ImageIO.read(optimizedFile), PNG_EXTENSION, workingFilePng);
						optimizedFilePng = new ExecutePngOptimization(workingFilePng, workingFilePng, conversionType).executeOptimization();
					} catch(final Exception e) {
						logger.debug("Unable to convert optimized GIF to PNG. Ignoring.", new ImageFileOptimizationException(optimizedFile.getPath(), e));
						ImageUtils.convertImageNative(optimizedFile, workingFilePng);
					}

					try {
						//First try optimizing the PNG version of the optimized GIF
						ImageIO.write(ImageIO.read(workingFile), PNG_EXTENSION, workingFilePng2);
						optimizedFilePng = new ExecutePngOptimization(workingFilePng2, workingFilePng2, conversionType).executeOptimization();
					} catch(final Exception e) {
						logger.debug("Unable to convert optimized GIF to PNG. Ignoring.", new ImageFileOptimizationException(workingFile.getPath(), e));
						ImageUtils.convertImageNative(workingFile, workingFilePng2);
					}

					final File optimizedFilePng2 = new ExecutePngOptimization(workingFilePng2, workingFilePng2, conversionType).executeOptimization();

					if((optimizedFilePng == null)) {
						workingFilePng.delete();
						if(!optimizedFilePng2.renameTo(workingFilePng)) {
							throw ImageFileOptimizationException.getInstance(workingFilePng, (Throwable)null);
						}
						optimizedFilePng = workingFilePng;
					} else if((optimizedFilePng.length() > optimizedFilePng2.length())) {
						workingFilePng.delete();
						if(!optimizedFilePng2.renameTo(workingFilePng)) {
							throw ImageFileOptimizationException.getInstance(workingFilePng, (Throwable)null);
						}
						optimizedFilePng = workingFilePng;
					} else {
						workingFilePng.delete();
						if(!optimizedFilePng.renameTo(workingFilePng)) {
							throw ImageFileOptimizationException.getInstance(workingFilePng, (Throwable)null);
						}
						optimizedFilePng = workingFilePng;
					}

					if(optimizedFilePng.length() < optimizedFile.length()) {
						fileTypeChanged = true;
						optimizedFile = optimizedFilePng;
					}
				} 

				final long masterFileSize = masterFile.length();
				if(optimizedFile.length() < masterFileSize) {
					final File finalFile = copyFileToMinifiedDirectory(masterFile, optimizedFile, fileTypeChanged);
					if(finalFile == null) {
						return null;
					}
					final boolean automatedOptimizationFailed;
					try {
						automatedOptimizationFailed = fileTypeChanged ? false : !ImageUtils.visuallyCompare(masterFile, optimizedFile);
					} catch(final ImageFileOptimizationException ifoe) {
						final Throwable cause = ifoe.getCause();
						if((cause instanceof NullPointerException) && "getImageTypes".equals(cause.getStackTrace()[0].getMethodName())) {
							logger.debug("The optimized image is corrupted and could not be read.", ifoe);
							return null;
						}
						throw ifoe;
					}

					return new OptimizationResult<>(finalFile, finalFile.length(), masterFile, masterFileSize, fileTypeChanged, automatedOptimizationFailed, false);
				}
			} catch(final ThirdPartyBinaryNotFoundException tpbnfe) {
				throw tpbnfe;
			} catch (final Exception e) {
				logger.warn(GIF_ERROR_MESSAGE, new ImageFileOptimizationException(masterFile.getPath(), e));
			} finally {
				try {
					FileUtils.forceDelete(workingFile.getParentFile());
				} catch (final IOException ioe) {
					logger.warn("Error deleting temp file.", ioe);
				}
			}

			return null;
		}
	}

	/**
	 * @see com.salesforce.perfeng.uiperf.imageoptimization.service.IImageOptimizationService#getFinalResultsDirectory()
	 */
	@Override
	public String getFinalResultsDirectory() {
		return finalWorkingDirectoryPath;
	}
	
	/**
	 * Called when the service is being shutdown, so it shuts down the thread 
	 * pool.
	 */
	public void destroy() {
		executorService.shutdown();
		logger.debug("The executorService is shutdown.");
	}
}
