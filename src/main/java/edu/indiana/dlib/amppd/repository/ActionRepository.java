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

	List<ActionBrief> findByActionTypeInAndTargetTypeIn(List<ActionType> actionTypes, List<TargetType> targetTypes);	
	List<ActionBrief> findByHttpMethodInAndUrlPatternIn(List<HttpMethod> httpMethods, List<String> urlPatterns);
	
}
