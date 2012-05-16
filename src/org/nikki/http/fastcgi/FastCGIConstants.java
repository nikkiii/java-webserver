/**
 * JavaHttpd, the flexible Java webserver
 * Copyright (C) 2012 Nikki <nikki@nikkii.us>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.nikki.http.fastcgi;

/**
 * A class containing all the FastCGI Constants
 * 
 * @author Nikki
 * 
 */
public class FastCGIConstants {

	/**
	 * Value for version component of FCGI_Header
	 */
	
	public static final int FCGI_VERSION = 1;

	
	/**
	 * Values for type component of FCGI_Header
	 */
	
	public static final int FCGI_BEGIN_REQUEST = 1;

	public static final int FCGI_ABORT_REQUEST = 2;

	public static final int FCGI_END_REQUEST = 3;

	public static final int FCGI_PARAMS = 4;

	public static final int FCGI_STDIN = 5;

	public static final int FCGI_STDOUT = 6;

	public static final int FCGI_STDERR = 7;

	public static final int FCGI_DATA = 8;

	public static final int FCGI_GET_VALUES = 9;

	public static final int FCGI_GET_VALUES_RESULT = 10;
	
	public static final int FCGI_UNKNOWN_TYPE = 11;

	
	/**
	 * Mask for flags component of FCGI_BeginRequestBody
	 */
	
	public static final int FCGI_KEEP_CONN = 1;

	
	/**
	 * Values for role component of FCGI_BeginRequestBody
	 */
	
	public static final int FCGI_RESPONDER = 1;
	
	public static final int FCGI_AUTHORIZER = 2;
	
	public static final int FCGI_FILTER = 3;
	
	
	/**
	 * Values for protocolStatus component of FCGI_EndRequestBody
	 */
	
	public static final int FCGI_REQUEST_COMPLETE = 0;
	
	public static final int FCGI_CANT_MPX_CONN = 1;
	
	public static final int FCGI_OVERLOADED = 2;
	
	public static final int FCGI_UNKNOWN_ROLE = 3;

}
