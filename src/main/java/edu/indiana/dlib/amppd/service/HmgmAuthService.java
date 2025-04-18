package edu.indiana.dlib.amppd.service;

public interface HmgmAuthService {
	public static String AUTH_SEPARATOR = ";;;;";

	/**
	 * Validate authentication info for an HMGM editor, by comparing the client supplied auth string to the 
	 * sha-256 authString pre-generated based on the given HMGM editor input file path and user entered password.
	 * if valid, return the HMGM token generated with the authentication info; otherwise return null.
	 * @param editorInput client supplied editor input file path
	 * @param userPass user entered password specific to the HMGM task
	 * @param authString client supplied auth string to compare against the pre-generated authString
	 * @return the generated HMGM token for HMGM editor authentication if valid; null otherwise
	 */
	public String validateAuthString(String editorInput, String userPass, String authString);
		
	/**
	 * Validate the supplied HMGM token by validating the authentication info wrapped inside it.
	 * If valid, return the HMGM token; otherwise return null.
	 * @param the supplied HMGM token
	 * @return the supplied HMGM token if valid; null otherwise
	 */
	public String validateHmgmToken(String hmgmToken);

	/**
	 * Validate the supplied HMGM token along with the supplied editor input and auth string.
	 * If the token is valid and consistent with the given editor input and auth string, return it; otherwise return null.
	 * @param the supplied HMGM token
	 * @param editorInput the supplied editor input
	 * @param authString the supplied auth string 
	 * @return the supplied HMGM token if valid; null otherwise
	 */
	public String validateHmgmTokenAuthString(String hmgmToken, String editorInput, String authString);

}