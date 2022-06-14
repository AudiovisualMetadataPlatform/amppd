package edu.indiana.dlib.amppd.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResultFilterCollection extends WorkflowResultFilterUnit {
	Long collectionId;
	String collectionName;	
}
