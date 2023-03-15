package edu.indiana.dlib.amppd.web;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for role assignment within a particular unit, containing user, role, and assignment info.
 * Note: For user, either ID or username must be provided; for role, either ID or name must be provided.
 * @author yingfeng
 *
 */
@Data
@NoArgsConstructor
public class RoleAssignTuple {

	private Long userId;
	private Long roleId;
	private String userName;
	private String roleName;
	private boolean assigned;
	
}
