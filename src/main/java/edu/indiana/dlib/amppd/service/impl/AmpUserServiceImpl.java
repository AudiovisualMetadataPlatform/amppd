package edu.indiana.dlib.amppd.service.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.AmpUser.Status;
import edu.indiana.dlib.amppd.model.TimedToken;
import edu.indiana.dlib.amppd.model.projection.AmpUserBrief;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.TimedTokenRepository;
import edu.indiana.dlib.amppd.security.JwtTokenUtil;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.RoleAssignService;
import edu.indiana.dlib.amppd.util.MD5Encryption;
import edu.indiana.dlib.amppd.web.AuthResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of AmpUserService.
 */ 
@Slf4j
@Service
public class AmpUserServiceImpl implements AmpUserService, UserDetailsService {

	  public static final int MIN_PASSWORD_LENGTH = 8;
	  public static final int MIN_USERNAME_LENGTH = 3;
	  public static final int MIN_NAME_LENGTH = 1;
	  public static final String ADMIN_FIRST_NAME = "ADMIN";
	  public static final String ADMIN_LAST_NAME = "AMP";	  

	  @Autowired
	  private AmppdPropertyConfig amppdPropertyConfig;		

	  @Autowired
	  private AmppdUiPropertyConfig amppdUiPropertyConfig;
		
	  @Autowired
	  private AmpUserRepository ampUserRepository;
	  
	  @Autowired
	  private TimedTokenRepository timedTokenRepository;		
	  
	  @Autowired
	  private JwtTokenUtil jwtTokenUtil;
	  
	  @Autowired
	  private JavaMailSender mailSender;
	  
	  @Autowired
	  private RoleAssignService roleAssignService;

	  private String adminEmail;
	  private String uiUrl;	  
	  
	  
	  @PostConstruct
	  public void init() {
		  adminEmail = amppdPropertyConfig.getAdminEmail();
		  log.trace("Fetched AMP admin email id from property file: "+ adminEmail);
		  uiUrl = amppdUiPropertyConfig.getUrl();
		  
		  // Note: bootstrap of AMP admin user is now moved to AmppdStartupRunner.run;
	  } 

	  @Override
	  public AuthResponse authenticate(String username, String password) { 
		  AuthResponse response = new AuthResponse();
		  
		  // password must be of valid format
		  if(!passwordAcceptableLength(password)) {
			  response.addError("Username and password do not match");
		  }
		  
		  // find user with credentials
		  String encryptedPswd = MD5Encryption.getMd5(password);
		  AmpUserBrief user = ampUserRepository.findFirstByUsernameAndPasswordAndStatus(username, encryptedPswd, AmpUser.Status.ACTIVATED);  
		  
		  // if user found, generate JWT token and authentication succeeded
		  if (user != null) {
				final String token = jwtTokenUtil.generateToken(username);
				response.setToken(token);
				response.setUser(user);
				response.setSuccess(true);
				log.info("User " + username + " login succeeded.");
		  }
		  // otherwise authentication failed
		  else {
			  response.setSuccess(false);
			  log.error("User " + username + " login failed.");
		  }
		  
		  return response;
	  }

	  @Override
	  @Transactional	
	  public AuthResponse registerAmpUser(AmpUser user) { 		  
		  AuthResponse response = new AuthResponse();
		  response.setEmailid(user.getEmail());

		  // validate user registration info
		  if(!usernameAcceptableLength(user.getUsername())) {
			  response.addError("Username must be " + MIN_USERNAME_LENGTH + " characters");
		  }		  
		  if(!nameAcceptableLength(user.getLastName())) {
			  response.addError("Last name must be " + MIN_USERNAME_LENGTH + " characters");
		  }	  
		  if(!nameAcceptableLength(user.getFirstName())) {
			  response.addError("First name must be " + MIN_USERNAME_LENGTH + " characters");
		  }	  
		  if(!emailUnique(user.getEmail())) {
			  response.addError("Email already exists");
		  }
		  if(!usernameUnique(user.getUsername())) {
			  response.addError("Username already exists");
		  }
		  if(!passwordAcceptableLength(user.getPassword())) {
			  response.addError("Password must be " + MIN_PASSWORD_LENGTH + " characters");
		  }
		  if(!validEmailAddress(user.getEmail())) {
			  response.addError("Invalid email address");
		  }

		  // return error response if validation failed
		  if (response.hasErrors()) {
			  log.error("Failed to validate user registration info and no account was created for user: " + user.getEmail());			  
			  return response;
		  }
		  
		  // otherwise, continue with registration process
		  try {
			  // encrypt password and save user
			  user.setPassword(MD5Encryption.getMd5(user.getPassword()));
			  user = ampUserRepository.save(user);
			  log.info("Successfully created user account <" + user.getId() + ">: " + user.getEmail());
			  
			  try {
				  // send email to AMP admin for account approval
				  mailSender.send(constructEmailAttributes(uiUrl, user, "account requested"));
				  response.setSuccess(true);
				  log.info("Successflly sent registration approval email for user " + user.getId());
			  } 
			  catch (MailException e) {
				  // in case email fails to be sent
				  response.setSuccess(false);
				  response.addError("Failed to send registration approval email for user " + user.getId());
				  log.warn("Failed to send registration approval email for user " + user.getId(), e);
			  }
		  }
		  catch (Exception e) {
			  // in case user account fails to be saved to DB
			  response.addError("Failed to create account for user " + user.getEmail());
			  log.error("Failed to create account for user " + user.getEmail(), e);
		  }

		  return response;
	  }
	  	  
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AmpUser user = ampUserRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found: " + username));
		GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), Arrays.asList(authority));
	}

	@Override
	public AuthResponse emailResetPasswordToken(String emailid) {
		AuthResponse response = new AuthResponse();
		log.info("Executing emailToken() for user:"+emailid);
		try {
			AmpUser user = ampUserRepository.findByEmail(emailid).orElseThrow(() -> new RuntimeException("User not found"));
			if(user.getStatus() == AmpUser.Status.ACTIVATED){
				log.info("Activated user account found with entered email id");
				SimpleMailMessage email =  constructTokenEmail(uiUrl, user, "reset password");
//				mailSender.send(email);
				log.info("Token email sent successfully");
			}
		    else {
		    	response.addError("User account status is invalid");
		    	response.setSuccess(false);
		    }
			response.setSuccess(true);
		}
		catch(Exception e){
			log.error("Failed to update reset password token for user " + emailid, e);
			response.addError(e.getMessage());
			response.setSuccess(false);
		}
		return response;
	}
	
	@Override
	@Transactional
	public AuthResponse resetPassword(String emailid, String new_password, String token) {
		AuthResponse response = new AuthResponse();
		AmpUser user = ampUserRepository.findByEmail(emailid).orElseThrow(() -> new RuntimeException("User not found: " + emailid));
		TimedToken passToken = (timedTokenRepository.findByToken(token)).orElseThrow(() -> new RuntimeException("token not found: " + token));
		if ((passToken == null) || (user == null) || (!passToken.getUser().getId().equals(user.getId()))) {
			log.error("Error occurred as token and email id do not match");
			response.addError("Invalid reset password link!");
			response.setSuccess(false);
			return response;
		}
		
		Calendar cal = Calendar.getInstance();
		if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
			log.error("Error occurred as reset password link has expired");
			log.trace("passToken.getExpiryDate().getTime():"+passToken.getExpiryDate().getTime()+","+cal.getTime().getTime());
			response.addError("Reset password link has expired!");
			response.setSuccess(false);
			return response;
		}	
		
		String new_encrypted_pswd = MD5Encryption.getMd5(new_password);
		int rows = ampUserRepository.updatePassword(user.getUsername(), new_encrypted_pswd, user.getId()); 

		if(rows > 0){
			response.setSuccess(true);
		}
		else {
			response.addError("Failed to reset password!");
			log.error("Error occurred while updating new password.");		  
		}
		
		return response;
	}
	
	@Override 
	@Transactional
	public AuthResponse approveAccount(Long userId, Boolean approve) {
		// retrieve user
		AuthResponse response = new AuthResponse();
		AmpUser user = ampUserRepository.findById(userId).orElse(null);
		String action = approve ? "approve" : "reject";
		
		// if user not found, return in error
		if (user == null){
			response.addError("Failed to " + action + " user account registration: invalid userId " + userId);
			response.setSuccess(false);
			log.error("Failed to " + action + " user account registration: invalid userId " + userId);
			return response;
		}
		
		// otherwise check account action
		response.setEmailid(user.getEmail());
		Status status = null;
		String notice = ""; 
		
		// for account approval
		if (action.equalsIgnoreCase("approve")) {
			status = Status.ACCEPTED;
			notice = "account approval";
		}
		// for account rejection
		else if (action.equalsIgnoreCase("reject")) {
			status = Status.REJECTED;
			notice = "account rejection";
		}
		// for invalid action, return in error
		else {
			response.addError("Failed to take action on user account " + userId + ": invalid action: " + action);
			response.setSuccess(false);
			log.error("Failed to take action on user account " + userId + ": invalid action: " + action);
			return response;
		}
		
		// for valid action, continue account action
		try {
			// update user account status 
			user.setStatus(status);
			ampUserRepository.updateStatus(userId, status);	
			
			// send email notification to user
			if (action.equalsIgnoreCase("approve")) {
				mailSender.send(constructTokenEmail(uiUrl, user, notice));
			}
			else {
				mailSender.send(constructEmailAttributes(uiUrl, user, notice));
			}
			
			response.setSuccess(true);
			log.info("Successfully " + action + "ed account registration for useer " + userId);			
		}
		catch (MailException e) {
			// in case user notice email fails to be sent
			response.setSuccess(false);
			response.addError("Failed to send " + notice + " email for user " + user.getId());
			log.warn("Failed to send " + notice + " email for user " + user.getId(), e);
		}
		catch (Exception e) {
			// in case any other exception
			response.addError("Failed to " + action + " account registration for useer " + userId);
			response.setSuccess(false);
			log.error("Failed to " + action + " account registration for useer " + userId, e);			
		}
				
		return response;		
	}
	
	@Override
	public AuthResponse resetPasswordGetEmail(String token){
		AuthResponse response = new AuthResponse();
		TimedToken passToken = (timedTokenRepository.findByToken(token)).orElseThrow(() -> new RuntimeException("token not found: " + token));
		if ((passToken == null)) {
			log.error("incorrect Link in resetPasswordGetEmail");
			response.addError("Incorrect Link");
			response.setSuccess(false);
		    }
		else {
			AmpUser user = ampUserRepository.findById(passToken.getUser().getId()).orElseThrow(() -> new RuntimeException("User not found: " + passToken.getId()));
			if(user!= null && user.getStatus() == AmpUser.Status.ACTIVATED){
				response.setEmailid(user.getEmail());
				response.setSuccess(true);
				log.info("Email fetched successfully");
			}
			else {
				response.addError("User doesn't exist");
				response.setSuccess(false);
			}
		}
		return response;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.AmpUserService.getCurrentUsername()
	 */
	@Override
	public String getCurrentUsername() {
		// if authentication is turned off and no login, then userDetails will be a String with value anonymousUser
		// in this case, use the default AMPPD user as current user; otherwise even authentication property is set to false,
		// user can still go through login, in which case userDetails will be the current AmpUser
		// in unit tests however, authentication could be null during setup, so an extra NP checking is needed
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();		
		Object userDetails = auth == null ? null : auth.getPrincipal();
		AmpUser user = userDetails != null && userDetails instanceof AmpUser ? (AmpUser) userDetails : null;
		String username = user != null && StringUtils.isNotEmpty(user.getUsername()) ? user.getUsername() : amppdPropertyConfig.getUsername();
		return username;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.AmpUserService.getCurrentUser()
	 */
	@Override
	public AmpUser getCurrentUser() {
		String username = getCurrentUsername();
		AmpUser currentUser = getUser(username);		
		if (currentUser == null) {
			throw new RuntimeException("Current user with username " + username + " doesn't exist!");
		}
		return currentUser;
	}
	
	@Override
	@Transactional
	public AuthResponse activateAccount(String token)
	{
		AuthResponse response = new AuthResponse();
		TimedToken passToken = (timedTokenRepository.findByToken(token)).orElseThrow(() -> new RuntimeException("token not found: " + token));
		if ((passToken == null)) {
			log.error("incorrect token for account activation");
			response.addError("Expired Url");
			response.setSuccess(false);
		}
		else {
			log.info("Matching token found.");
			Calendar cal = Calendar.getInstance();
		    if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
		    	log.error("Token expired!!");
		    	response.addError("Link Expired");
				response.setSuccess(false);
		    }
		    else {
		    	log.info("Token expiration validated.");
				AmpUser user = ampUserRepository.findById(passToken.getUser().getId()).orElseThrow(() -> new RuntimeException("User not found: " + passToken.getId()));
				if(user!= null) {
					if(user.getStatus() == AmpUser.Status.ACCEPTED){
						log.debug("Corresponding amp user was approved for activation.");
						try {
							ampUserRepository.updateStatus(user.getId(), AmpUser.Status.ACTIVATED);
							response.setSuccess(true);
							log.info("User activated successfully");
						}
						catch (Exception ex) {
							log.error(ex.getMessage());
							response.setSuccess(false);
							response.addError("System encountered error");
						}
					}
					else if(user.getStatus() == AmpUser.Status.ACTIVATED) {
						log.error("User account already active");
						response.setSuccess(false);
						response.addError("User account already active");
					}
				}
				else {
					log.error("user not found for the activation token received");
					response.addError("User doesn't exist");
					response.setSuccess(false);
				}
		    }
		}
		return response;
	}
	
	//HELPER FUNCTIONS FOLLOW ---->
	
	private SimpleMailMessage constructTokenEmail(String contextPath, AmpUser user, String type) {
		String url = new String();
    	if (type.equalsIgnoreCase("reset password")) {
    		String token = createTimedToken(user, amppdPropertyConfig.getResetPasswordMinutes());
    		url = contextPath + "/account/reset-password/" + token;
    		log.debug("Constructed reset password token url: " + url);
    	}
    	else if (type.equalsIgnoreCase("account approval")) {
    		String token = createTimedToken(user, amppdPropertyConfig.getActivateAccountDays() * 24 * 60);
    		url = contextPath + "/account/activate/" + token;
    		log.debug("Constructed account activation token url: " + url);
    	}	
	    return constructEmailAttributes(url, user, type);
	}
	
	private SimpleMailMessage constructEmailAttributes(String contextPath, AmpUser user, String type) {
		String subject = null;
		String emailTo = null;
		String message = null;
		String url = null;
		if (type.equalsIgnoreCase("account requested")){
			log.debug("Constructing email for user account request: " + user.getUsername());
			url = contextPath + "/account/approve/" + user.getId();
			message = "A new user has registered and waiting approval. \n\n User Name: " + user.getUsername() + "\n User Email: "+user.getEmail()+ "\n User ID: "+user.getId()+
					"\n\n Click the link below to view and approve the new user. \n";
			subject = amppdPropertyConfig.getEnvironment() + ": New User account request: " + user.getUsername();
			emailTo = adminEmail;
		}
		else if (type.contentEquals("reset password"))
		{
			log.debug("Constructing Email for reset password request: " + user.getUsername());
			url = contextPath;
			message = "Please click the link to reset the password. The link  will be valid only for a limited time.";
			subject = "Reset Account Password";
			emailTo = user.getEmail();			
		}
		else if (type.equalsIgnoreCase("account rejection")){
			log.debug("Constructing Email for User account rejection notification: " + user.getUsername());
			url = "" ;
			message = "Sign up rejected. Please reply to this email if you think this was done in error.";
			subject = "Sign up rejected";
			emailTo = user.getEmail();
		}
		else if (type.equalsIgnoreCase("account approval")){
			log.debug("Constructing email for User account activation: " + user.getUsername());
			url = contextPath;
			message = "Your registration request has been reviewed and accepted. \n Click the link below to activate your AMP account";
			subject = "Activate your account";
			emailTo = user.getEmail();
		}
		log.debug("Constructed email attributes: type: " + type + ", from: " + adminEmail + ", to: " + emailTo);
		return constructEmail(subject, message + " \r\n" + url, emailTo);
	}
	
	@Transactional
	public String createTimedToken(AmpUser user, int expireMinutes) {
		int res = 0;
		String token = UUID.randomUUID().toString();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, expireMinutes);
		
		// retrieve existing token if any
		TimedToken tt  = timedTokenRepository.findFirstByUserId(user.getId());
		
		if (tt == null) {
			tt = new TimedToken();
			tt.setUser(user);
		}
		
		// save token
		tt.setToken(token);
		tt.setExpiryDate(calendar.getTime());
		tt = timedTokenRepository.save(tt);
		
		log.debug("Generated timed token for user " + user.getUsername());
		return token;
	}
	
	private SimpleMailMessage constructEmail(String subject, String body, String toEmailID) {
	    SimpleMailMessage email = new SimpleMailMessage();
	    email.setSubject(subject);
	    email.setText(body);
	    email.setTo(toEmailID);
	    email.setFrom(adminEmail);
	    log.debug("Constructed Email Object with all the information packed: " + body);
	    return email;
	}  

	public AmpUser getUser(String username) {
		Optional<AmpUser> userOpt = ampUserRepository.findByUsername(username);
		if(userOpt.isPresent()) return userOpt.get();		
		return null;
	}
	
	@Override
	public AmpUser getUserById(Long userId) {
		AmpUser user= ampUserRepository.findById(userId).orElseThrow(() -> new StorageException("User not found: " + userId));
		log.info("User fetched Successfully");
		return user;		
	}

	public AmpUser getUserByEmail(String email) {
		Optional<AmpUser> userOpt = ampUserRepository.findByEmail(email);
		if(userOpt.isPresent()) return userOpt.get();
		return null;
	}
	  
	@Transactional
	public boolean activateUser(String username) {
		try {
			AmpUser user = ampUserRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found: " + username));
			if(user!=null)
				ampUserRepository.updateStatus(user.getId(), AmpUser.Status.ACTIVATED);
		}
		catch(Exception ex) {
			  System.out.println(ex.toString());
			  return false;
		}
		return true;		    
	}
	
	private boolean emailUnique(String email) {
		return !ampUserRepository.emailExists(email);  
	}

	private boolean usernameUnique(String username) {
		return !ampUserRepository.usernameExists(username);  
	}
	
	private boolean usernameAcceptableLength(String username) {
		return username.length() >= MIN_USERNAME_LENGTH;
	}
	
	private boolean nameAcceptableLength(String name) {
		return name.length() >= MIN_NAME_LENGTH;
	}
	  
	private boolean passwordAcceptableLength(String password) {
		return password.length() >= MIN_PASSWORD_LENGTH;
	}
	  
	private boolean validEmailAddress(String email) {
		Pattern pattern = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");     
		Matcher matcher = pattern.matcher(email);		  
		return matcher.matches();
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.AmpUserService.bootstrapAdminUser()
	 */
	@Override
    @Transactional
	public AmpUser bootstrapAdmin() {
		String username = amppdPropertyConfig.getUsername();
		String password = amppdPropertyConfig.getPassword();
		String encryppw = MD5Encryption.getMd5(password);
		AmpUser admin = getUser(username);
		
		// create admin user if not existing yet
		if (admin == null) {
			admin = new AmpUser();
			admin.setUsername(username);
			admin.setPassword(encryppw);
			admin.setEmail(adminEmail);
			admin.setFirstName(ADMIN_FIRST_NAME);
			admin.setLastName(ADMIN_LAST_NAME);
			log.info("Initializing the new AMP admin account ...");
		}
		// otherwise verify the existing admin account info
		else {
			// Note:
			// For usual AMP users, the username is the same as the email address;
			// For AMP admin, we intentionally configure the username not in email address pattern, 
			// so as to avoid possible conflict with user registration.
			// However, if the admin account info is changed manually in DB i.e. not by AMP app (strongly recommended against), 
			// the inconsistency could cause potential issues, in which case, a warning is given below.
			// The alternative is to override DB info with config info, but we chose not to do so for now.			
			if (!StringUtils.equals(encryppw, admin.getPassword()) ||
					!StringUtils.equals(adminEmail, admin.getEmail()) ||
					!StringUtils.equals(ADMIN_FIRST_NAME, admin.getFirstName()) ||
					!StringUtils.equals(ADMIN_LAST_NAME, admin.getLastName())) {
				log.warn("The AMP admin user account already exists, but its password, email, or name is different from the configuration. Please verify/update your configuration to be consistent with the database.");			
			}			
		}
		
		// for new admin or existing admin account which somehow didn't get activated, update the status and save to repository
		if (admin.getStatus() != AmpUser.Status.ACTIVATED) {
			admin.setStatus(AmpUser.Status.ACTIVATED);
			ampUserRepository.save(admin);
			log.info("Activated the new or existing AMP admin account to be ready for use.");
		}
		else {
			log.info("AMP admin alaredy exists and has been activated.");
		}
		
		// assign AMP Admin role 
		roleAssignService.assignAdminRole(admin);
		return admin;
	}
	
}
