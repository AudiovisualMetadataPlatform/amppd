package edu.indiana.dlib.amppd.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class containing fields corresponding to the Galaxy workflow creation request body. 
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GalaxyWorkflowRequest {
	private String workflow_name = "Unknow Workflow";
	private String workflow_annotation = "";	
}
