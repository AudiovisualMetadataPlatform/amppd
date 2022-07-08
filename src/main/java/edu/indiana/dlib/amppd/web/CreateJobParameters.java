package edu.indiana.dlib.amppd.web;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class wrapping the parameters map passed in as one of the request parameters for workflow submission. 
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobParameters {
	
	// map of (step, (paramName, paramValue)) 
	private Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
	
}
