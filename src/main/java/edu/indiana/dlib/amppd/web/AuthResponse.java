package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;

import lombok.Data;
@Data
public class AuthResponse {
	public AuthResponse() {
		errors = new ArrayList<String>();
	}
	private boolean success;
	private ArrayList<String> errors;
	
	public void addError(String error) {
		errors.add(error);
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
}
