package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.User;
import edu.indiana.dlib.amppd.web.AuthResponse;

/**
 * Service for User related functions.
 * @author vinitab
 *
 */

public interface UserService {

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
	public AuthResponse registerUser(User user);
	
	/**
	 * Sends an email.
	 * @param user contains new user info to be sent in the email
	 * @return the result of email sending as success/failure
	 */
	void sendEmail(User u);

	/**
	 * Sets amp user as approved to login to the application
	 * @param user name
	 * @return the result of setting the user to approved as success/failure
	 */
	boolean approveUser(String userName);
}
