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
package com.salesforce.perfeng.uiperf;

import java.util.MissingResourceException;

/**
 * Exception to indicate that a 3rd party binary that is required by the 
 * code cannot be found. This usually indicates a setup issue.
 */
public class ThirdPartyBinaryNotFoundException extends MissingResourceException {
	private static final long serialVersionUID = -1656740462957955952L;

	/**
     * Constructs a {@code ThirdPartyBinaryNotFoundException} with the 
     * specified binary application name and addition information for fixing the
     * issue.
     * 
     * @param binaryApplicationName
     *        The name of the binary application that could not be found
     * @param additionalFixMessage 
     *        This <code>String</code> should include additional steps needed to
     *        fix the issue, such as anything that needs to be installed on the 
     *        OS.
     */
    public ThirdPartyBinaryNotFoundException(final String binaryApplicationName, final String additionalFixMessage) {
    	super("The binary application \"" + binaryApplicationName + "\" cannot be launched. " + ((additionalFixMessage == null) ? "" : additionalFixMessage), binaryApplicationName, binaryApplicationName);
	}
	
	/**
     * Constructs a {@code ThirdPartyBinaryNotFoundException} with the 
     * specified binary application name.
     *
     * @param binaryApplicationName
     *        The name of the binary application that could not be found
     */
    public ThirdPartyBinaryNotFoundException(final String binaryApplicationName) {
    	this(binaryApplicationName, (String)null);
    }
	
    /**
     * Constructs a {@code ThirdPartyBinaryNotFoundException} with the 
     * specified binary application name and addition information for fixing the
     * issue.
     * 
     * @param binaryApplicationName
     *        The name of the binary application that could not be found
     * @param additionalFixMessage 
     *        This <code>String</code> should include additional steps needed to
     *        fix the issue, such as anything that needs to be installed on the 
     *        OS.
     * @param cause 
     *        The cause (which is saved for later retrieval by the
     */
    public ThirdPartyBinaryNotFoundException(final String binaryApplicationName, final String additionalFixMessage, final Throwable cause) {
    	this(binaryApplicationName, additionalFixMessage);
    	this.initCause(cause);
	}
    
	/**
     * Constructs a {@code ThirdPartyBinaryNotFoundException} with the 
     * specified binary application name and cause.
     *
     * @param binaryApplicationName
     *        The name of the binary application that could not be found
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     */
    public ThirdPartyBinaryNotFoundException(final String binaryApplicationName, final Throwable cause) {
    	this(binaryApplicationName, null, cause);
    }
}