package org.nikki.http.module;

/**
 * An exception which can be thrown while loading/unloading modules
 * 
 * @author Nikki
 *
 */
public class ModuleLoadingException extends Exception {
	
	private static final long serialVersionUID = 8722123881811102854L;

	public ModuleLoadingException(Throwable throwable) {
		super(throwable);
	}
	
	public ModuleLoadingException(String string) {
		super(string);
	}
}
