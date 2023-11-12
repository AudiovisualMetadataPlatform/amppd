package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;

import edu.indiana.dlib.amppd.model.projection.AmpUserBrief;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
	
	private boolean success;
	private ArrayList<String> errors = new ArrayList<String>();
	private String emailid;	// same as username
	private String token;
	private AmpUserBrief user;
	
	public AuthResponse(String emailid) {
		this.emailid = emailid;
		errors = new ArrayList<String>();
	}
	
	public void addError(String error) {
		errors.add(error);
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	@Override
	public String toString() {
		String str = "AuthResponse";
		str += "<success: " + success;
		str += ", emailid: " + emailid;
		str += ", token: " + token;
		str += ", errors: [";
		for (String error : errors) {
			str += error + ", ";
		}
		str += "]>";
		return str;
	}
}