package edu.indiana.dlib.amppd.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResultFilterItem extends WorkflowResultFilterCollection {	
	Long itemId;
	String itemName;	
	String externalId;
	String externalSource;	
}
