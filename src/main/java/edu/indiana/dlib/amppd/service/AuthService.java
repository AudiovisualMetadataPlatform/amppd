package edu.indiana.dlib.amppd.service;

public interface AuthService {

	/**
	 * Compares input values to a sha-256 auth string
	 * @param authString SHA-256 hash string
	 * @param userToken User supplied token
	 * @param editorInput Input file path
	 * @return
	 */
	boolean compareAuthStrings(String authString, String userToken, String editorInput);
	
	/**
	 * Check to see if it is an authorized user, first based on JWT set user, then token.
	 * @param authString
	 * @param userToken
	 * @param editorInput
	 * @return
	 */
	boolean isAuthorized(String authString, String userToken, String editorInput);

}