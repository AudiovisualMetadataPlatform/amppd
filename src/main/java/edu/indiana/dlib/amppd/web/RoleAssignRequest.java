package edu.indiana.dlib.amppd.web;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request containing user, role, and assignment info.
 * @author yingfeng
 *
 */
@Data
@NoArgsConstructor
public class RoleAssignRequest {

	private Long userId;
	private Long roleId;
	private Boolean assigned;
	
}
