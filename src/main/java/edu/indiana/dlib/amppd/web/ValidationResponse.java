package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import edu.indiana.dlib.amppd.service.model.Manifest;
import lombok.Data;

@Data
public class ValidationResponse {

	public ValidationResponse() {
		errors = new ArrayList<String>();
	}
	
	boolean success;
	Manifest manifest;
	
	
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
