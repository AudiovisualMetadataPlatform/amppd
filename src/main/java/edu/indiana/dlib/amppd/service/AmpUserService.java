package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.web.AuthResponse;

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
	public AuthResponse validate(String username, String pswd);
	
	/**
	 * Register the new user and make entry to database.
	 * @param user contains new user data
	 * @return the result of registration
	 */
	public AuthResponse registerAmpUser(AmpUser user);
	
	/**
	 * Sends an email.
	 * @param user contains new user info to be sent in the email
	 * @return the result of email sending as success/failure
	 */
	//void sendEmail(AmpUser u);

	/**
	 * Sets amp user as approved to login to the application
	 * @param user name
	 * @return the result of setting the user to approved as success/failure
	 */
	boolean approveUser(String userName);
	
	/**
	 * Resets the existing password in the database for the given username
	 * @param username, new password to be updated in the DB, secure token
	 * @return the boolean result for update in the databse
	 */
	public AuthResponse resetPassword(String userName, String new_password, String token);
	
	/**
	 * Generates a token and sends it in an email to the provided email id
	 * @param email id
	 * @return the boolean result for sending the email
	 */
	public AuthResponse emailToken(String emailid);
}
