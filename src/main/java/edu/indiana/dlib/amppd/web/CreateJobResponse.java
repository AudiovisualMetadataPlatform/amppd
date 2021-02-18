package edu.indiana.dlib.amppd.web;

import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import lombok.Data;

@Data
public class CreateJobResponse {
	private Boolean success;
	private String error;
	private Long primaryfileId;
	private String collectionName = "";
	private String itemName = "";
	private String primaryfileName = "";
	private WorkflowOutputs outputs;
	
	public CreateJobResponse() {
	}

	public CreateJobResponse(Long primaryfileId) {
		this.primaryfileId = primaryfileId;
	}
	
	/**
	 * Sets the identifying name fields with the information from the given primaryfile.
	 * @param primaryfile
	 */
	public void setNames(Primaryfile primaryfile) {
		Item item = primaryfile.getItem();
		Collection c = item.getCollection();	
		setCollectionName(c.getName());
		setPrimaryfileName(primaryfile.getName());
		setItemName(item.getName());
	}
	
	/**
	 * Sets the status fields with the given success status, error message, and workflow outputs.
	 * @param success
	 * @param error
	 * @param outputs
	 */
	public void setStatus(Boolean success, String error, WorkflowOutputs outputs) {
		this.success = success;
		this.error = error;
		this.outputs = outputs;
	}
		
}
