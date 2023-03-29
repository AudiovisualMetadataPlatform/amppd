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
import edu.indiana.dlib.amppd.model.TimedToken;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.TimedTokenRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
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
	  private JavaMailSender mailSender;

	  private String adminEmail ;
	  private String uiUrl ;	  
	  
	  @PostConstruct
	  public void init() {
		  adminEmail = amppdPropertyConfig.getAdminEmail();
		  log.trace("Fetched AMP admin email id from property file:"+adminEmail);
		  uiUrl = amppdUiPropertyConfig.getUrl();
		  
		  // Note: bootstrap of AMP admin user is now moved to AmppdStartupRunner.run;
	  } 

	  @Override
	  public AuthResponse authenticate(String username, String pswd) { 
		  AuthResponse response = new AuthResponse();
		  
		  if(!passwordAcceptableLength(pswd)) {
			  response.addError("Username and password do not match");
		  }
		  String encryptedPswd = MD5Encryption.getMd5(pswd);
		  String userFound = ampUserRepository.findByApprovedUser(username, encryptedPswd, AmpUser.Status.ACTIVATED);  
		  if (userFound != null) {
			  response.setSuccess(true);
			  log.info("User " + username + " validated Successfully");
		  }
		  else {
			  response.setSuccess(false);
			  log.error("User " + username + " validation failed");
		  }
		  return response;
	  }

	  @Override
	  @Transactional	
	  public AuthResponse registerAmpUser(AmpUser user) { 
		  
		  AuthResponse response = new AuthResponse();
		  		  
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
		  
		  if(!response.hasErrors()) {
			  user.setPassword(MD5Encryption.getMd5(user.getPassword()));
			  user = ampUserRepository.save(user);
			  if(user!=null && user.getId() > 0) 
			  {
				  log.info("User validated successfully. Registration email being sent");
				  try {
					  mailSender.send(constructEmailAttributes(uiUrl, user, "account requested"));
					} 
				  catch (MailException e) {
					  e.printStackTrace();
				  	}
					response.setSuccess(true);
			  }
			  else {
				  response.addError("Error creating user account");
				  log.error("User validation unsuccessful so not registering the user: "+user.getEmail());
			  }
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
				mailSender.send(constructTokenEmail(uiUrl, user, "reset password"));
				log.info("Token email sent successfully");
			}
		    else {
		    	response.addError("User account status is invalid");
		    	response.setSuccess(false);
		    }
			response.setSuccess(true);
		}
		catch(Exception e){
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
			response.addError("Incorrect Link");
			response.setSuccess(false);
		    }
		Calendar cal = Calendar.getInstance();
	    if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
	    	log.error("Error occurred as link has expired");
	    	log.trace("passToken.getExpiryDate().getTime():"+passToken.getExpiryDate().getTime()+","+cal.getTime().getTime());
	    	response.addError("Link Expired");
			response.setSuccess(false);
	    }		
	    if(!response.hasErrors()) {
			  String new_encrypted_pswd = MD5Encryption.getMd5(new_password);
			  int rows = ampUserRepository.updatePassword(user.getUsername(), new_encrypted_pswd, user.getId()); 
			  log.error("Errors occurred in the password reset process");
			  if(rows > 0){
				  response.setSuccess(true);
			  }
		  }
		  return response;
	}
	
	@Override 
	@Transactional
	public AuthResponse accountAction(Long userId, String action){
		AuthResponse response = new AuthResponse();
		AmpUser user = ampUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
		if(user == null){
			response.addError("Unauthorized Link");
			response.setSuccess(false);
		}
		if(!response.hasErrors()) {
			try {
				if(action.contentEquals("approve")){
					user.setStatus(AmpUser.Status.ACCEPTED);
					mailSender.send(constructTokenEmail(uiUrl, user, "account approval"));
				}
				else if(action.contentEquals("reject")){
					mailSender.send(constructEmailAttributes(uiUrl, user, "account rejection"));
					user.setStatus(AmpUser.Status.REJECTED);
				}
				
			}
			catch (Exception e) {
				response.addError("Account action:"+action+" could not be completed");
				response.setSuccess(false);
				return response;
			}
			int rows = ampUserRepository.updateStatus(userId, user.getStatus());
			if(rows > 0){
				response.setSuccess(true);
			}
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
						log.info("Corresponding amp user was found.");
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
    	log.info("Password reset token created successfully");
    	if (type.equalsIgnoreCase("reset password")) {
    		String token = createTimedToken(user, amppdPropertyConfig.getResetPasswordMinutes());
    		url = contextPath + "/account/reset-password/" + token;
    		log.info("Constructed reset token url, constructing email attributes");
    	}
    	else if (type.equalsIgnoreCase("account approval")) {
    		String token = createTimedToken(user, amppdPropertyConfig.getActivateAccountDays() * 24);
    		url = contextPath + "/account/activate/" + token;
    		log.info("Constructed activation token url, constructing email attributes");
    	}	
	    return constructEmailAttributes(url, user, type);
	}
	
	private SimpleMailMessage constructEmailAttributes(String contextPath, AmpUser user, String type) {
		String subject = null;
		String emailTo = null;
		String message = null;
		String url = null;
		if (type.equalsIgnoreCase("account requested")){
			log.info("constructing Email for User account request:"+user.getUsername());
			url = contextPath + "/account/approve/" + user.getId();
			message = "A new user has registered and waiting approval. \n\n User Name:"+ user.getUsername()+"\n User Email: "+user.getEmail()+ "\n User ID: "+user.getId()+
					"\n\n Click the link below to view and approve the new user. \n";
			subject = amppdPropertyConfig.getEnvironment() + ": New User account request: " + user.getUsername();
			emailTo = adminEmail;
		}
		else if(type.contentEquals("reset password"))
		{
			log.info("constructing Email for reset password request:"+user.getUsername());
			url = contextPath;
			message = "Please click the link to reset the password. The link  will be valid only for a limited time.";
			subject = "Reset Account Password";
			emailTo = user.getEmail();			
		}
		else if (type.equalsIgnoreCase("account rejection")){
			log.info("constructing Email for User account rejection notification:"+user.getUsername());
			url = "" ;
			message = "Sign up rejected. Please reply to this email if you think this was done in error.";
			subject = "Sign up rejected";
			emailTo = user.getEmail();
		}
		else if (type.equalsIgnoreCase("account approval")){
			log.info("Constructing email for User account activation"+user.getEmail());
			url = contextPath;
			message = "Your registration request has been reviewed and accepted. \n Click the link below to activate your AMP account";
			subject = "Activate your account";
			emailTo = user.getEmail();
		}
		log.debug("Sending email from email id:"+adminEmail);
		return constructEmail(subject, message + " \r\n" + url, emailTo);
	}
	
	@Transactional
	public String createTimedToken(AmpUser user, int expireMinutes) {
		int res = 0;
		String token = UUID.randomUUID().toString();
		TimedToken myToken=new TimedToken();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, expireMinutes);
		int userTokenExists = timedTokenRepository.ifExists(user.getId());
		log.info("User token exists status is:"+userTokenExists+" for user id:"+user.getId());
		if(userTokenExists >= 1){
			log.info("User token exists so updating token info");
			res = timedTokenRepository.updateToken(token, user.getId(), calendar.getTime());
		}
		else {
			log.info("User token does not exist so creating new token");
			myToken.setUser(user);
			myToken.setToken(token);
			myToken.setExpiryDate(calendar.getTime());
			myToken = timedTokenRepository.save(myToken);
			if(myToken != null)
				res = 1;
		}
		log.info("The result of creating a token is (1:true/0:false):"+res);
		if(res > 0)
			return token;
		else
			return null;
	}
	
	private SimpleMailMessage constructEmail(String subject, String body, String toEmailID) {
	    SimpleMailMessage email = new SimpleMailMessage();
	    email.setSubject(subject);
	    email.setText(body);
	    email.setTo(toEmailID);
	    email.setFrom(adminEmail);
	    log.info("Constructed Email Object with all the information packed.");
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
		return admin;
	}
	
}
