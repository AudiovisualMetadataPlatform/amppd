package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;

import edu.indiana.dlib.amppd.model.projection.AmpUserBrief;
import lombok.Data;
@Data
public class AuthResponse {
	public AuthResponse() {
		errors = new ArrayList<String>();
	}
	
	private boolean success;
	private ArrayList<String> errors;
	private String emailid;
	private String token;
	private AmpUserBrief user;
	
	public void addError(String error) {
		errors.add(error);
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
}