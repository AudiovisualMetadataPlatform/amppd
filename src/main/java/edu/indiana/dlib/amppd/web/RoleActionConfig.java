package edu.indiana.dlib.amppd.web;

import java.util.List;

import edu.indiana.dlib.amppd.model.projection.ActionBrief;
import edu.indiana.dlib.amppd.model.projection.RoleBriefActions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Response for role_action permission configuration (global or unit-scope) retrieval request, containing actions and roles with permissions.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleActionConfig {

	// names of all predefined unit-scope roles, applicable only to unit-scope configuration
	private List<String> unitRoleNames;

	// roles for which the current user can view/config action permissions 
	private List<RoleBriefActions> roles;	
	
	// actions for which the current user can view/config action permissions
	private List<ActionBrief> actions;

}
