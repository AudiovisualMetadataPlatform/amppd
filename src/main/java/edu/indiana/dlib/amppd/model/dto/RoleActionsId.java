package edu.indiana.dlib.amppd.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for role with actions projection, used for role_action configuration.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleActionsId {
	
    private Long id;
    private String name;
    private List<Long> actionIds;
    
}
