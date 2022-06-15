package edu.indiana.dlib.amppd.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//not using superclass because lombok @AllArgsConstructor doesn't handle super fields
public class WorkflowResultFilterCollection { // extends WorkflowResultFilterUnit {
	Long unitId;
	String unitName;		
	Long collectionId;
	String collectionName;		
}
