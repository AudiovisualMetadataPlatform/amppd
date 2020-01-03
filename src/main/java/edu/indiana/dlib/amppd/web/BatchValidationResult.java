package edu.indiana.dlib.amppd.web;

import java.util.List;

import lombok.Data;

@Data
public class BatchValidationResult {	
	List<String> errors;
	public void addError(String errorMessage) {
		errors.add(errorMessage)
;	}
}
