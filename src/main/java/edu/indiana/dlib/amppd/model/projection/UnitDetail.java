package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;


/**
 * Projection for a detailed view of a unit.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Unit.class}) 
public interface UnitDetail extends UnitBrief, DataentityDetail {

	public Set<CollectionBrief> getCollections();
	public Set<UnitSupplementBrief> getSupplements();
	public Set<RoleAssignment> getRoleAssignments();
	
}
