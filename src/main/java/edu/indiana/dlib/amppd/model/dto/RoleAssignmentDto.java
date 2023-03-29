package edu.indiana.dlib.amppd.model.dto;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for RoleAssignment brief DTO.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentDto {

    private Long id;
    private Long userId;
    private Long roleId;    
    private Long unitId;
    private String username;    
    private String roleName;    
    private String unitName;    

    public RoleAssignmentDto(RoleAssignment ra) {
    	id = ra.getId();
    	userId = ra.getUser().getId();
    	roleId = ra.getRole().getId();
    	unitId = ra.getUnit() == null ? null : ra.getUnit().getId();
    	username = ra.getUser().getUsername();
    	roleName = ra.getRole().getName();
    	unitName = ra.getUnit() == null ? null : ra.getUnit().getName();
    }
    
    public RoleAssignmentDto(RoleAssignmentDetail rad) {
    	id = rad.getId();
    	userId = rad.getUserId();
    	roleId = rad.getRoleId();
    	unitId = rad.getUnitId();
    	username = rad.getUsername();
    	roleName = rad.getRoleName();
    	unitName = rad.getUnitName();
    }    
    
}
