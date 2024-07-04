package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import edu.indiana.dlib.amppd.model.Batch;
import lombok.Data;

@Data
public class BatchResponse {
	private boolean success;	
	private Batch batch;	
	private List<String> validationErrors;	
	private List<String> processingErrors;
	
	public BatchResponse() {
		validationErrors = new ArrayList<String>();
		processingErrors = new ArrayList<String>();
	}
	
	public boolean hasValidationErrors() {
		return !validationErrors.isEmpty();
	}
	
	public void addValidationError(String error) {
		validationErrors.add(error);
	}

	public void addValidationErrors(List<String> errors) {
		if(validationErrors!=null && !errors.isEmpty()) {
			validationErrors.addAll(errors);
		}
	}
	
	public boolean hasProcessingErrors() {
		return !processingErrors.isEmpty();
	}

	public void addProcessingError(String error) {
		processingErrors.add(error);
	}
	
	public void addProcessingErrors(List<String> errors) {
		if(processingErrors!=null && !errors.isEmpty()) {
			processingErrors.addAll(errors);
		}
	}
	
}