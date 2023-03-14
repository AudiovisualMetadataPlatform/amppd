package edu.indiana.dlib.amppd.web;

import java.util.List;

import edu.indiana.dlib.amppd.model.dto.AmpUserDto;
import edu.indiana.dlib.amppd.model.dto.RoleDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for role assignment retrieval request, containing roles, users, and assignments for a unit.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignResponse {
	
	// assignable role level threshold for the current user in the current unit
	private Integer level;
	
	// roles for which the current user can view assignments in the current unit
	private List<RoleDto> roles;	

//	// roles for which the current user can update assignments in the current unit
//	private List<Role> assignableRoles;
	
	// users currently assigned with roles in the current unit
	private List<AmpUserDto> users;
	
	// 2D Boolean array (list of boolean arrays), with rows of users and columns of roles, and cells indicating whether the user has the role 
	private List<boolean[]> assignments;
	
	
}
