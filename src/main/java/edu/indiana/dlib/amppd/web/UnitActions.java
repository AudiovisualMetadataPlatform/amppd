package edu.indiana.dlib.amppd.web;

import java.util.Set;

import edu.indiana.dlib.amppd.model.projection.ActionBrief;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class containing actions the current user can perform within a particular unit.
 * @author yingfeng
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitActions {
	
	private Long unitId;
	private Set<ActionBrief> actions;

}
