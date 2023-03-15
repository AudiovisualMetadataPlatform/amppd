package edu.indiana.dlib.amppd.web;

import java.util.List;

import edu.indiana.dlib.amppd.model.projection.AmpUserBrief;
import edu.indiana.dlib.amppd.model.projection.RoleBrief;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for role assignment retrieval request, containing roles, users, and assignments table within a unit.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignTable {
	
	// assignable role level threshold for the current user in the current unit
	private Integer level;
	
	// roles for which the current user can view assignments in the current unit
	private List<RoleBrief> roles;	

//	// roles for which the current user can update assignments in the current unit
//	private List<RoleBrief> assignableRoles;
	
	// users currently assigned with roles in the current unit
	private List<AmpUserBrief> users;
	
	// 2D Boolean array (list of boolean arrays), with rows of users and columns of roles, and cells indicating whether the user has the role 
	private List<boolean[]> assignments;
	
	
}
