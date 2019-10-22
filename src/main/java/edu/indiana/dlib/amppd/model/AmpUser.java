package edu.indiana.dlib.amppd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AmpUser {
	
	private String username;
	@JsonIgnore
	private String password;
}
