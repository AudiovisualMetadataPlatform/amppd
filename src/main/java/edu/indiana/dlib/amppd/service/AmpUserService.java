package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.AmpUser;

/**
 * Service for AmpUser related functions.
 * @author vinitab
 *
 */

public interface AmpUserService {

	/**
	 * Validate the login credentials against database.
	 * @param username is the entered username
	 * @param pswd is the entered password
	 * @return the result of validation
	 */
	public boolean validate(String username, String pswd);
	
	/**
	 * Register the new user and make entry to database.
	 * @param user contains new user data
	 * @return the result of registration
	 */
	public boolean registerAmpUser(AmpUser user);
	
	/**
	 * Sends an email.
	 * @param user contains new user info to be sent in the email
	 * @return the result of email sending as success/failure
	 */
	void sendEmail(AmpUser u);
}
