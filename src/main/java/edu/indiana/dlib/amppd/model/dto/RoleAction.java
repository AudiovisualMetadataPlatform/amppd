package edu.indiana.dlib.amppd.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Class used only for parsing role-action csv file to refresh the static permissions in the role_action table. 
 * It's not persisted or correspond to the actual role_action table. 
 * @author yingfeng
 *
 */
@Data
@EqualsAndHashCode()
@ToString(callSuper=true)
public class RoleAction {

    private String roleName;
    private String actionName;
    
}
