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
package com.salesforce.perfeng.uiperf.imageoptimization.dto;

import java.io.File;
import java.util.Objects;

/**
 * This class holds the results of optimizing 1 image.
 *
 * @author eperret (Eric Perret)
 * @param <C> The changelist object.
 * @since 186.internal
 */
public class OptimizationResult<C> {

    private final File optimizedFile;
    private final long optimizedFileSize;
    private final File originalFile;
    private final long originalFileSize;
    private final boolean fileTypeChanged;
    private final boolean failedAutomatedTest;
    private final boolean isBrowserSpecific;
    private C newChangeList;
    private String gusBugId;
    private String ownerUserName;

    /**
     * Constructor which sets all of the values.
     *
     * @param optimizedFile The optimized version of the image
     * @param optimizedFileSize The size of the optimized image (aka
     *                          {@link File#length()}
     * @param originalFile The original version of the image
     * @param originalFileSize The size of the original image (aka
     *                         {@link File#length()}
     * @param fileTypeChanged {@code true} if the file type of the optimized
     *                        image is different than the file type of the
     *                        original images. This usually is {@code true} when
     *                        converting a GIF to PNG because it is small in
     *                        length.
     * @param failedAutomatedTest {@code true} if the file failed the automated
     *                            validation after compression and should be
     *                            considered ineligible for check-in. If
     *                            {@code false} then we have a valid optimized
     *                            image.
     * @param isBrowserSpecific {@code true} if the image format only works in 1
     *                          type of browser.
     */
    public OptimizationResult(final File optimizedFile, final long optimizedFileSize, final File originalFile, final long originalFileSize, final boolean fileTypeChanged, final boolean failedAutomatedTest, final boolean isBrowserSpecific) {
        this.optimizedFile = optimizedFile;
        this.optimizedFileSize = optimizedFileSize;
        this.originalFile = originalFile;
        this.originalFileSize = originalFileSize;
        this.fileTypeChanged = fileTypeChanged;
        this.failedAutomatedTest = failedAutomatedTest;
        this.isBrowserSpecific = isBrowserSpecific;
    }

    /**
     * @return Returns the optimizedFile
     */
    public File getOptimizedFile() {
        return optimizedFile;
    }

    /**
     * @return Returns the optimizedFileSize
     */
    public long getOptimizedFileSize() {
        return optimizedFileSize;
    }

    /**
     * @return Returns the originalFile
     */
    public File getOriginalFile() {
        return originalFile;
    }

    /**
     * @return Returns the originalFileSize
     */
    public long getOriginalFileSize() {
        return originalFileSize;
    }

    /**
     * @return Returns the fileTypeChanged
     */
    public boolean isFileTypeChanged() {
        return fileTypeChanged;
    }

    /**
     * @return Returns if the optimized image is smaller than the original
     *         image.
     */
    public boolean isOptimized() {
        return optimizedFileSize < originalFileSize;
    }

    /**
     * @return Returns the failedAutomatedTest
     */
    public boolean isFailedAutomatedTest() {
        return failedAutomatedTest;
    }

    /**
     * @return The changlist object used to commit this change back to the
     *         version repository system.
     */
    public C getNewChangeList() {
        return newChangeList;
    }

    /**
     * @param changelist The changelist number object used to commit this change
     *                   back to the version repository system.
     */
    public void setNewChangeList(final C changelist) {
        newChangeList = changelist;
    }

    /**
     * @return the gusBugId
     */
    public final String getGusBugId() {
        return gusBugId;
    }

    /**
     * @param gusBugId the gusBugId to set
     */
    public final void setGusBugId(final String gusBugId) {
        this.gusBugId = gusBugId;
    }

    /**
     * @return the ownerUserName
     */
    public final String getOwnerUserName() {
        return ownerUserName;
    }

    /**
     * @param ownerUserName the ownerUserName to set
     */
    public final void setOwnerUserName(final String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }

    /**
     * @return the isBrowserSpecific
     */
    public final boolean isBrowserSpecific() {
        return isBrowserSpecific;
    }

    /**
     * Eclipse generated with a subset of the fields
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(failedAutomatedTest, fileTypeChanged, isBrowserSpecific, gusBugId, newChangeList, optimizedFile, optimizedFileSize, originalFile, originalFileSize);
    }

    /**
     * Eclipse generated with a subset of the fields
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final OptimizationResult<?> other = (OptimizationResult<?>) obj;
        if (failedAutomatedTest != other.failedAutomatedTest) {
            return false;
        }
        if (fileTypeChanged != other.fileTypeChanged) {
            return false;
        }
        if (isBrowserSpecific != other.isBrowserSpecific) {
            return false;
        }
        if (!Objects.equals(gusBugId, other.gusBugId)) {
            return false;
        }
        if (!Objects.equals(newChangeList, other.newChangeList)) {
            return false;
        }
        if (!Objects.equals(optimizedFile, other.optimizedFile)) {
            return false;
        }
        if (optimizedFileSize != other.optimizedFileSize) {
            return false;
        }
        if (!Objects.equals(originalFile, other.originalFile)) {
            return false;
        }
        if (originalFileSize != other.originalFileSize) {
            return false;
        }
        return true;
    }

    /**
     * Returns the human readable version of the data.
     *
     * @return Text
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(originalFile.getName());
        if(fileTypeChanged) {
            sb.append(" --> ").append(optimizedFile.getName());
        }
        return sb.append("\n\tfailedAutomatedTest:\t").append(failedAutomatedTest)
                .append("\n\tfileTypeChanged:\t").append(fileTypeChanged)
                .append("\n\tisBrowserSpecific:\t").append(isBrowserSpecific)
                .append("\n\toriginalFileSize:\t").append(originalFileSize)
                .append("\n\toptimizedFileSize:\t").append(optimizedFileSize)
                .append("\n\tSavings:\t\t").append(originalFileSize - optimizedFileSize).toString();
    }
}