package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import edu.indiana.dlib.amppd.model.Batch;
import lombok.Data;

@Data
public class BatchValidationResponse {
	private boolean success;	
	private Batch batch;	
	private List<String> validationErrors;	
	private List<String> processingErrors;
	
	public BatchValidationResponse() {
		validationErrors = new ArrayList<String>();
		processingErrors = new ArrayList<String>();
	}
	
	public void addError(String error) {
		validationErrors.add(error);
	}
	public void addErrors(List<String> errors) {
		if(validationErrors!=null && !errors.isEmpty()) {
			validationErrors.addAll(errors);
		}
	}
	
	public void addProcessingError(String error) {
		processingErrors.add(error);
	}
	public void addProcessingErrors(List<String> errors) {
		if(processingErrors!=null && !errors.isEmpty()) {
			processingErrors.addAll(errors);
		}
	}
	
	public boolean hasErrors() {
		return !validationErrors.isEmpty();
	}
	
	public boolean hasProcessingErrors() {
		return !processingErrors.isEmpty();
	}
}