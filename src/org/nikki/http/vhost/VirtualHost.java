package org.nikki.http.vhost;

import java.io.File;

/**
 * A basic Virtual Host for document roots
 * 
 * @author Nikki
 * 
 */
public class VirtualHost {

	/**
	 * The domain name of this vhost
	 */
	private String domainName;

	/**
	 * The document root
	 */
	private File documentRoot;

	/**
	 * Create a new VirtualHost
	 * 
	 * @param domainName
	 *            The domain name
	 * @param documentRoot
	 *            The document root
	 */
	public VirtualHost(String domainName, File documentRoot) {
		this.domainName = domainName;
		this.documentRoot = documentRoot;
	}

	/**
	 * Get the domain name
	 * 
	 * @return The domain name
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * Get the document root
	 * 
	 * @return The document root
	 */
	public File getDocumentRoot() {
		return documentRoot;
	}

	@Override
	public String toString() {
		return "VirtualHost [domainName=" + domainName + ", documentRoot="
				+ documentRoot + "]";
	}
}
