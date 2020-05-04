package edu.indiana.dlib.amppd.service.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.config.AmppdUiPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Passwordresettoken;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.PasswordTokenRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.util.MD5Encryption;
import edu.indiana.dlib.amppd.web.AuthResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AmpUserServiceImpl implements AmpUserService, UserDetailsService {

	  private int MIN_PASSWORD_LENGTH = 8;
	  private int MIN_USERNAME_LENGTH = 3;

	  @Autowired
	  private AmpUserRepository ampUserRepository;
	  
	  @Autowired
	  private PasswordTokenRepository passwordTokenRepository;
	  
	  @Autowired
	  private AmppdPropertyConfig amppdConfig;		
		
	  @NotNull
	  private static String ampEmailId ;
	  
	  @NotNull
	  private static String ampAdmin ;
	  
	  @NotNull
	  private static String uiUrl ;
	  
	  @Autowired
	  private JavaMailSender mailSender;
	  
	  
	  @Autowired 
	  public AmpUserServiceImpl(AmppdPropertyConfig amppdconfig, AmppdUiPropertyConfig amppduiConfig) { 
		  ampEmailId = amppdconfig.getUsername();
		  ampAdmin = amppdconfig.getAdmin();
		  uiUrl = amppduiConfig.getUrl();
	  } 

	  public AuthResponse validate(String email, String pswd) { 
		  AuthResponse response = new AuthResponse();
		  
		  if(!passwordAcceptableLength(pswd)) {
			  response.addError("Email and password do not match");
		  }
		  String encryptedPswd = MD5Encryption.getMd5(pswd);
		  String userFound = ampUserRepository.findByApprovedUser(email, encryptedPswd, AmpUser.State.ACCEPTED);  
		  if(userFound != null)
		  {
			  if(userFound.equals("1")) {
				  response.setSuccess(true);
			  }
			  log.info("User validated Successfully");
		  }
		  return response;
	  }
	  
	  public AmpUser getUser(String username) {
		Optional<AmpUser> userOpt = ampUserRepository.findByUsername(username);
		if(userOpt.isPresent()) return userOpt.get();
		
		return null;
	  }

	  public AmpUser getUserByEmail(String email) {
		Optional<AmpUser> userOpt = ampUserRepository.findByEmail(email);
		if(userOpt.isPresent()) return userOpt.get();
		
		return null;
	  }
	  
	  public boolean approveUser(String username) {
		  try {
			AmpUser user = ampUserRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found: " + username));
			/*
			 * user.setApprove_status(AmpUser.State.ACCEPTED); ampUserRepository.save(user);
			 */
			if(user!=null)
				ampUserRepository.updateApprove_status(user.getId(), AmpUser.State.ACCEPTED);
		  }
		  catch(Exception ex) {
			  System.out.println(ex.toString());
		  }
		  return false;		    
	  }
	  
	  public AuthResponse registerAmpUser(AmpUser user) { 
		  
		  AuthResponse response = new AuthResponse();
		  		  
		  if(!usernameAcceptableLength(user.getUsername())) {
			  response.addError("Username must be " + MIN_USERNAME_LENGTH + " characters");
		  }
		/*
		 * else if(!usernameUnique(user.getUsername())) {
		 * response.addError("Username already taken."); }
		 */
		  
		  if(!emailUnique(user.getEmail())) {
			  response.addError("Email already exists");
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
					  mailSender.send(constructRegisterEmail(uiUrl, user, "approve"));
					} 
				  catch (MailException e) {
					  e.printStackTrace();
				  	}
					response.setSuccess(true);//new GenericResponse(messages.getMessage("message.resetPasswordEmail", null, request.getLocale()));
			  }
			  else {
				  response.addError("Error creating user account");
				  log.error("User validation unsuccessful so not registering the user: "+user.getEmail());
			  }
		  }
		  
		  return response;
	  }
	  
	  private boolean usernameUnique(String username) {
		  return !ampUserRepository.usernameExists(username);  
	  }
	  
	  private boolean emailUnique(String username) {
		  return !ampUserRepository.emailExists(username);  
	  }

	  private boolean usernameAcceptableLength(String username) {
		  return username.length() >= MIN_USERNAME_LENGTH;
	  }
	  
	  private boolean passwordAcceptableLength(String password) {
		  return password.length() >= MIN_PASSWORD_LENGTH;
	  }
	  
	  private boolean validEmailAddress(String email) {
		  Pattern pattern = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");  
		   
		  Matcher matcher = pattern.matcher(email);
		  
		  return matcher.matches();
	  }
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AmpUser user = ampUserRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found: " + username));
		GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());
		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Arrays.asList(authority));
	}

	@Override
	public AuthResponse emailToken(String emailid) {
		// TODO Auto-generated method stub
		AuthResponse response = new AuthResponse();
		log.info("Executing emailToken() for user:"+emailid);
		try {
			AmpUser user = ampUserRepository.findByEmail(emailid).orElseThrow(() -> new RuntimeException("User not found"));
			
			
			if(user.getApprove_status()==AmpUser.State.ACCEPTED)
			{
				log.info("Approved user found with entered email id");
				String token = UUID.randomUUID().toString();
				boolean res = createPasswordResetTokenForUser(user, token);
			    if(res)
			    {
			    	log.info("Password reset token created successfully");
					try {
				    	mailSender.send(constructResetTokenEmail(uiUrl, token, user));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					response.setSuccess(true);
					log.info("Token email sent successfully");
			    }
			    else
			    {
			    	response.setSuccess(false);
			    }
			}
		}
		catch(Exception e)
		{
			response.addError(e.getMessage());
			response.setSuccess(false);
		}
		return response;
	}
	
	private SimpleMailMessage constructRegisterEmail(String contextPath, AmpUser user, String type) {
		String subject = null;
		String emailTo = null;
		String message = null;
		String url = null;
		if (type.equalsIgnoreCase("approve"))
		{
			log.info("constructing Email for User account approval:"+user.getUsername());
			url = contextPath + "/approve-user/" + user.getId();
			message = "A new user has registered and waiting approval. \n\n User Name:"+ user.getUsername()+"\n User Email: "+user.getEmail()+ "\n User ID: "+user.getId()+
					"\n\n Click the link below to view and approve the new user. \n";
			subject = "New User Approval";
			emailTo = ampAdmin;
		    
		}
		else if (type.equalsIgnoreCase("rejected"))
		{
			log.info("constructing Email for User account rejection notification:"+user.getUsername());
			url = contextPath + "/" ;
			message = "Your registeration request has been reviewed and unfortunately it was not accepted by the admin.\n Please contact the AMP's admin for further details.";
			subject = "Registeration request rejected";
			emailTo = user.getEmail();
		    
		}
		else if (type.equalsIgnoreCase("activated"))
		{
			log.info("Constructing email for user account activation notification"+user.getUsername());
			url = contextPath + "/activate-account/" + user.getId();
			message = "Your registeration request has been reviewed and accepted. \n Click the link below to activate your AMP account";
			subject = "Activate Account";
			emailTo = user.getEmail();
		}
		return constructEmail(subject, message + " \r\n" + url, emailTo);
	}
	
	public boolean createPasswordResetTokenForUser(AmpUser user, String token) {
		int res = 0;
		Passwordresettoken myToken=new Passwordresettoken();
		Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
		calendar.add(Calendar.SECOND,Passwordresettoken.EXPIRATION);
		int userTokenExists = passwordTokenRepository.ifExists(user.getId());
		log.info("User token exists status is:"+userTokenExists+" for user id:"+user.getId());
		if(userTokenExists >= 1)
		{
			log.info("User token exists so updating token info");
			res = passwordTokenRepository.updateToken(token, user.getId(), calendar.getTime());
		}
		else
		{
			log.info("User token does not exist so creating new token");
			myToken.setUser(user);
			myToken.setToken(token);
			myToken.setExpiryDate(calendar.getTime());
			myToken = passwordTokenRepository.save(myToken);
			if(myToken != null)
				res = 1;
		}
		log.info("The result of creating a token is (1:true/0:false):"+res);
		if(res > 0)
			return true;
		else
			return false;
	}
	
	private SimpleMailMessage constructResetTokenEmail(String contextPath, String token, AmpUser user) {
		String url = contextPath + "/reset-password/" + token;
		String message = "Please click the link to reset the password. The link  will be valid only for a limited time.";//messages.getMessage("message.resetPassword", null, locale);
		log.info("Constructed reset token email :"+ message);
		return constructEmail("Reset Password", message + " \r\n" + url, user.getEmail());
	}
			 
	private SimpleMailMessage constructEmail(String subject, String body, String toEmailID) {
	    SimpleMailMessage email = new SimpleMailMessage();
	    email.setSubject(subject);
	    email.setText(body);
	    email.setTo(toEmailID);
	    email.setFrom(ampEmailId);
	    log.info("Constructed Email Object with all the information packed");
	    return email;
	}  

	@Override
	public AuthResponse resetPassword(String emailid, String new_password, String token) {
		// TODO Auto-generated method stub
		AuthResponse response = new AuthResponse();
		AmpUser user = ampUserRepository.findByEmail(emailid).orElseThrow(() -> new RuntimeException("User not found: " + emailid));
		Passwordresettoken passToken = (passwordTokenRepository.findByToken(token)).orElseThrow(() -> new RuntimeException("token not found: " + token));
		if ((passToken == null) || (user == null) || (passToken.getUser().getId() != user.getId())) {
			response.addError("Incorrect Link");
			response.setSuccess(false);
		    }
		Calendar cal = Calendar.getInstance();
	    if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
	    	response.addError("Link Expired");
			response.setSuccess(false);
	    }
		
	    if(!response.hasErrors()) {
			  String new_encrypted_pswd = MD5Encryption.getMd5(new_password);
			  int rows = ampUserRepository.updatePassword(user.getUsername(), new_encrypted_pswd, user.getId()); 
			  if(rows > 0)
			  {
				  response.setSuccess(true);
			  }
		  }
	    
		  return response;
	}
	
	@Override 
	public AuthResponse approveUser(Long userId, String action)
	{
		boolean approved = true;
		AuthResponse response = new AuthResponse();
		AmpUser user = ampUserRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
		
		if(user == null)
		{
			response.addError("Incorrect Link");
			response.setSuccess(false);
		}
		if(!response.hasErrors()) {
			try {
				if(action.contentEquals("approve"))
				{
					user.setApprove_status(AmpUser.State.ACCEPTED);
					mailSender.send(constructRegisterEmail(uiUrl, user, "activated"));
					
				}
				else if(action.contentEquals("reject"))
				{
					mailSender.send(constructRegisterEmail(uiUrl, user, "rejected"));
					user.setApprove_status(AmpUser.State.REJECTED);
				}
			}
			catch (Exception e) 
			{
				System.out.println("Entered exception handling for mailsender");
				response.addError("Couldn't send email to:"+user.getEmail());
				response.setSuccess(false);
				//e.printStackTrace();
				return response;
			}
			int rows = ampUserRepository.updateApprove_status(userId, user.getApprove_status());
			if(rows > 0)
			{
				response.setSuccess(true);
			}
		}
		return response;
	}
	
	@Override
	public AuthResponse resetPasswordGetEmail(String token)
	{
		AuthResponse response = new AuthResponse();
		Passwordresettoken passToken = (passwordTokenRepository.findByToken(token)).orElseThrow(() -> new RuntimeException("token not found: " + token));
		if ((passToken == null)) {
			log.error("incorrect Link in resetPasswordGetEmail");
			response.addError("Incorrect Link");
			response.setSuccess(false);
		    }
		else
		{
			AmpUser user = ampUserRepository.findById(passToken.getUser().getId()).orElseThrow(() -> new RuntimeException("User not found: " + passToken.getId()));
			if(user!= null && user.getApprove_status() == AmpUser.State.ACCEPTED)
			{
				response.setEmailid(user.getEmail());
				response.setSuccess(true);
				log.info("Email fetched successfully");
			}
			else
			{
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
		AmpUser userDetails = (AmpUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
		String username = userDetails.getUsername();
		return username;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.AmpUserService.getCurrentUser()
	 */
	@Override
	public AmpUser getCurrentUser() {
		// TODO replace below tmp code with logic to get the current user from User Session		
		// tmp code: return the default master AMP user for now
		String username = getCurrentUsername();
		AmpUser currentUser = getUser(username);		
		if (currentUser == null) {
			throw new RuntimeException("Current user with username " + username + " doesn't exist!");
		}
		return currentUser;
	}
	
}