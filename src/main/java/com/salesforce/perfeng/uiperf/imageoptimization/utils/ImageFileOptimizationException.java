package com.salesforce.perfeng.uiperf.imageoptimization.utils;

import java.io.File;
import java.io.IOException;

/**
 * Used to indicate that there was a problem trying to optimize an image.
 *
 * @author eperret (Eric Perret)
 * @since 186.internal
 */
public class ImageFileOptimizationException extends RuntimeException {
    private static final long serialVersionUID = 5182477811689374166L;

    /**
     * @param imagePath The path to the image.
     * @param cause The {@link Throwable} that caused this exception to occur.
     */
    public ImageFileOptimizationException(final String imagePath, final Throwable cause) {
        super("Error while optimizing the file \"" + imagePath + "\"", cause);
    }

    /**
     * @param imagePath The path to the image.
     * @param message The detail message indicating why the image optimization
     *                failed. The detail message is saved for later retrieval by
     *                the {@link #getMessage()} method.
     */
    public ImageFileOptimizationException(final String imagePath, final String message) {
        super("Error while optimizing the file \"" + imagePath + "\". " + message);
    }

    /**
     * @param imagePath The path to the image.
     * @param message The detail message indicating why the image optimization
     *                failed. The detail message is saved for later retrieval by
     *                the {@link #getMessage()} method.
     * @param cause The {@link Throwable} that caused this exception to occur.
     */
    public ImageFileOptimizationException(final String imagePath, final String message, final Throwable cause) {
        super("Error while optimizing the file \"" + imagePath + "\". " + message, cause);
    }

    /**
     * Creates a new instance of {@link ImageFileOptimizationException}
     *
     * @param image The image that is being processed.
     * @param cause What caused the image to fail the processing.
     * @return the newly created exception
     */
    public static final ImageFileOptimizationException getInstance(final File image, final Throwable cause) {
        String path;
        try {
            path = image.getCanonicalPath();
        } catch (final IOException ioe) {
            path = image.toString();
        }

        return new ImageFileOptimizationException(path, cause);
    }

    /**
     * Creates a new instance of {@link ImageFileOptimizationException}
     *
     * @param image The image that is being processed.
     * @param message The detail message indicating why the image optimization
     *                failed.
     * @return the newly created exception
     */
    public static final ImageFileOptimizationException getInstance(final File image, final String message) {
        String path;
        try {
            path = image.getCanonicalPath();
        } catch (final IOException ioe) {
            path = image.toString();
        }

        return new ImageFileOptimizationException(path, message);
    }

    /**
     * Creates a new instance of {@link ImageFileOptimizationException}
     *
     * @param image The image that is being processed.
     * @param message The detail message indicating why the image optimization
     *                failed.
     * @param cause What caused the image to fail the processing.
     * @return the newly created exception
     */
    public static final ImageFileOptimizationException getInstance(final File image, final String message, final Throwable cause) {
        String path;
        try {
            path = image.getCanonicalPath();
        } catch (final IOException ioe) {
            path = image.toString();
        }

        return new ImageFileOptimizationException(path, message, cause);
    }
}