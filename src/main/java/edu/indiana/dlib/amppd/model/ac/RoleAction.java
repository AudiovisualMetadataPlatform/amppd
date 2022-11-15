package edu.indiana.dlib.amppd.model.ac;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
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
