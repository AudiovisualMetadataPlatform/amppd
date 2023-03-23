package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.HttpMethod;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.projection.ActionBrief;

@RepositoryRestResource(excerptProjection = ActionBrief.class)
public interface ActionRepository extends AmpObjectRepository<Action> {

	Action findFirstByName(String name);
	Action findFirstByActionTypeAndTargetType(ActionType actionType, TargetType targetType);
	Action findFirstByHttpMethodAndUrlPattern(HttpMethod httpMethod, String urlPattern);

	// find all actions projected as ActionBrief
	List<ActionBrief> findBy();

	// find the actions for which the role permissions are or not configurable;
	// currently, Create/Delete Unit, update RoleAction or RoleAssignment are not configurable 
	List<ActionBrief> findByConfigurable(Boolean configurable);
	
	// find actions by actionType-targetType, httpMethod-UrlPattern 
	List<ActionBrief> findByActionTypeInAndTargetTypeIn(List<ActionType> actionTypes, List<TargetType> targetTypes);	
	List<ActionBrief> findByHttpMethodInAndUrlPatternIn(List<HttpMethod> httpMethods, List<String> urlPatterns);
	
//	// find the actions for which the role permissions are not configurable, i.e.
//	// Create/Delete Unit, update Role or RoleAssignment 
//	@Query(value = "select a from Action a where " +
//			"a.actionType in ('Create', 'Delete') and a.targetType = 'Unit' or " + 
//			"a.actionType = 'Update' and a.targetType like 'Role%'")			
//	List<ActionBrief> findNonConfigurableActions();
//
//	// find the actions for which the role permissions are configurable, i.e. except
//	// Create/Delete Unit, update Role or RoleAssignment 
//	@Query(value = "select a from Action a where " +
//			"(a.actionType not in ('Create', 'Delete') or a.targetType <> 'Unit') and " + 
//			"(a.actionType <> 'Update' or a.targetType not like 'Role%')")			
//	List<ActionBrief> findConfigurableActions();
	
}
