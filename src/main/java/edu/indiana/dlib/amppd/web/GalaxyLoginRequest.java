package edu.indiana.dlib.amppd.web;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class containing fields corresponding to the Galaxy login request body. 
 * @author yingfeng
 */
@Data
@AllArgsConstructor
public class GalaxyLoginRequest {
	private String login;
	private String password;
	private String url;
	private String redirect;
	private String session_csrf_token;
	private String messageText;
	private String messageVariant;
	private Boolean allowUserCreation;
	private Boolean enable_oidc;	
}
