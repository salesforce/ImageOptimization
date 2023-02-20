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

	/**
	 * Returns the extension required for binary applications. Throws an error if OS is not supported.
	 *
	 * @return The path relative to where the JVM is being run from.
	 */
	public static String getBinaryApplicationExtension() {
		final String os = System.getProperty("os.name").toLowerCase();
		if ("linux".equals(os)) {
			return "";
		}
		if ("mac os x".equals(os)) {
			return "";
		}
		if (os.startsWith("windows")) {
			return ".exe";
		}
		throw new UnsupportedOperationException(
			"Your OS is not supported by this application. Currently only Linux, MacOS X and Windows are supported");
	}

	/**
	 * Returns the default directory for the binary applications. Throws an error if OS is not supported.
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
		if (os.startsWith("windows")) {
			return "./lib/binary/windows/";
		}
		throw new UnsupportedOperationException(
			"Your OS is not supported by this application. Currently only Linux, MacOS X, and Windows are supported");
	}

	private ProcessUtil() {
		// Private to prevent developers from unnecessarily instantiating this
		// class.
	}
}
