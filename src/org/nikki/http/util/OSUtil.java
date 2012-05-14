package org.nikki.http.util;

public class OSUtil {
	/**
	 * An enum containing operating system types
	 * 
	 * @author Nikki
	 *
	 */
	public static enum OperatingSystem {
		LINUX, SOLARIS, WINDOWS, MAC, UNKNOWN
	}
	
	/**
	 * The cached operating system
	 */
	public static final OperatingSystem SYSTEM = getPlatform();

	/**
	 * Get the current platform
	 * @return
	 * 		The current platform
	 */
	public static OperatingSystem getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win"))
			return OperatingSystem.WINDOWS;
		if (osName.contains("mac"))
			return OperatingSystem.MAC;
		if (osName.contains("solaris"))
			return OperatingSystem.SOLARIS;
		if (osName.contains("sunos"))
			return OperatingSystem.SOLARIS;
		if (osName.contains("linux"))
			return OperatingSystem.LINUX;
		if (osName.contains("unix"))
			return OperatingSystem.LINUX;
		return OperatingSystem.UNKNOWN;
	}
}
