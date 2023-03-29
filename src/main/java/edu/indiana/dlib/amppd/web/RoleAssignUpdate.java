package edu.indiana.dlib.amppd.web;

import java.util.List;

import edu.indiana.dlib.amppd.model.dto.RoleAssignmentDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for role assignment update request within a particular unit, containing lists of role assignments added and deleted.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignUpdate {	
	
	// role assignments added
	private List<RoleAssignmentDto> added;
	
	// role assignments deleted
	private List<RoleAssignmentDto> deleted;
		
	// role assignments not updated, i.e. already assigned/unassigned
	private List<RoleAssignTuple> unchanged;

	// role assignments failed, due to none existing user/role/unit or current user not allowed to assign the role
	private List<RoleAssignTuple> failed;

}
