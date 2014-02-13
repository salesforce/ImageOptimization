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