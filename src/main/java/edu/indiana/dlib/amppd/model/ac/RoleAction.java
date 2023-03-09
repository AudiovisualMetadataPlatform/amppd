package edu.indiana.dlib.amppd.model.ac;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class is used only for parsing role-action csv file to refresh the static permissions in the role_action table. 
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
