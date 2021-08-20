package edu.indiana.dlib.amppd.web;

import lombok.Data;

/**
 * Validation error with field in error and the corresponding error message.
 * @author yingfeng
 */
@Data
public class ValidationError {
	
	  private final String field;
	  private final String message;
	  
}
