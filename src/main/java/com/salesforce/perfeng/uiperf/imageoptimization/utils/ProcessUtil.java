/**
 * 
 */
package com.salesforce.perfeng.uiperf.imageoptimization.utils;

/**
 * Utility for interacting with processes.
 * 
 * @author eperret (Eric Perret)
 */
public final class ProcessUtil {

    private ProcessUtil() {
        // Private to prevent developers from unnecessarily instantiating this
        // class.
    }
    
    /**
     * Returns the default directory for the binary applications. Throws an
     * error if OS is not supported.
     * 
     * @return The path relative to where the JVM is being run from.
     */
    public static String getDefaultBinaryAppLocation() {
        final String os = System.getProperty("os.name").toLowerCase();
        if ("linux".equals(os)) {
            return "./lib/binary/linux/";
        }
        if ("mac os x".equals(os)) {
            return "./lib/binary/darwin/";
        }
        throw new UnsupportedOperationException("Your OS is not supported by this application. Currently only Linux and MacOS X are supported");
    }
}
