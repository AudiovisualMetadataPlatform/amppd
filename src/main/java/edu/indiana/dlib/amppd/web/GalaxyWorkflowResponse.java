package edu.indiana.dlib.amppd.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class containing fields corresponding to the Galaxy workflow creation response body. 
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GalaxyWorkflowResponse {
	private String id;
	private String message;	
}
