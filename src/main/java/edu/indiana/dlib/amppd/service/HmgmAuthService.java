package edu.indiana.dlib.amppd.service;

public interface HmgmAuthService {
	public static String AUTH_SEPARATOR = ";;;;";

	/**
	 * Validate authentication info for HMGM editors, by validating the authentication wrapped inside the supplied HMGM token
	 * against the sha-256 authString pre-generated based on the authentication info.
	 * if valid, return the HMGM token; otherwise return null.
	 * @param the supplied HMGM token
	 * @return the generated HMGM token for HMGM editor authentication if valid; null otherwise
	 */
	public String validateHmgmToken(String hmgmToken);

	/**
	 * Validate authentication info for HMGM editors, by comparing the client supplied authString to the 
	 * sha-256 authString pre-generated based on the given HMGM editor input file path and user entered password.
	 * if valid, return the HMGM token generated with the authentication info; otherwise return null.
	 * @param editorInput client supplied editor input file path
	 * @param userPass user entered password specific to the HMGM task
	 * @param authString client supplied authString to compare against the pre-generated authString
	 * @return the generated HMGM token for HMGM editor authentication if valid; null otherwise
	 */
	public String validateAuthString(String editorInput, String userPass, String authString);
		
}