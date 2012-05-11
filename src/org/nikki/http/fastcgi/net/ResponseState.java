package org.nikki.http.fastcgi.net;

/**
 * The FastCGI Response states
 * 
 * @author Nikki
 *
 */
public enum ResponseState {
	VERSION,
	HEADER,
	CONTENT
}
