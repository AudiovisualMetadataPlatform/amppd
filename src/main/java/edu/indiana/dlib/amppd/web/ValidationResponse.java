package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ValidationResponse {

	public ValidationResponse() {
		errors = new ArrayList<String>();
	}
	private boolean success;
	private ArrayList<String> errors;
	
	public void addError(String error) {
		errors.add(error);
	}
	public void addErrors(List<String> errors) {
		if(errors!=null && !errors.isEmpty()) {
			this.errors.addAll(errors);
		}
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
}
