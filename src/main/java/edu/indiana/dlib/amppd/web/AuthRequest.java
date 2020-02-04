package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class AuthRequest {
	private Long id;
	private String username;
	private String password;
	private String emailid;
	private String token;
}
