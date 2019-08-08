package edu.indiana.dlib.amppd.model.galaxy;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

// TODO This class is not used anymore and can probably be removed.

/**
 * Wrapper class containing all the fields received when querying Galaxy Workflow vis REST API.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
public class GalaxyWorkflow {

	private String name;
	private String[] tags;
	private boolean deleted;
	private String latest_workflow_uuid;
	private boolean show_in_tool_panel;
	private String url;
	private int number_of_steps;
	private boolean published;
	private String owner;
	private String model_class;
	private String id;	
	
}
