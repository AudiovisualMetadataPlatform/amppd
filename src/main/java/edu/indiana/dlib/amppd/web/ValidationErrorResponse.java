package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Response containing a list of validation errors.
 * @author yingfeng
 */
@Data
public class ValidationErrorResponse {

	private List<ValidationError> validationErrors = new ArrayList<ValidationError>();
	
}
