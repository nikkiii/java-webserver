package org.nikki.http.util;

import java.util.Map;

public class ProcessTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ProcessBuilder pb = new ProcessBuilder("");
		Map<String, String> environment = pb.environment();
		environment.put("", "");
	}

}
