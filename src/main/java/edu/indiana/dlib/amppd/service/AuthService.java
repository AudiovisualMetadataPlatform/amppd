package edu.indiana.dlib.amppd.service;

public interface AuthService {
	public static String AUTH_SEPARATOR = ";;;;";

	/**
	 * Validate authentication info for HMGM editors, by comparing the client supplied authString to the 
	 * sha-256 authString pre-generated based on the given userToken and given HMGM editor input path.
	 * if valid, return a token generated with the authentication info; otherwise return null.
	 * @param editorInput user supplied editor input file path
	 * @param userToken user supplied token
	 * @param authString the supplied authString to compare against the pre-generated authString
	 * @return the generated token for HMGM editor authentication if valid; null otherwise
	 */
	public String validateAuthStrings(String editorInput, String userToken, String authString);
	

}