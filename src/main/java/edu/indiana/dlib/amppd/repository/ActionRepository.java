package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.HttpMethod;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;

@RepositoryRestResource()
public interface ActionRepository extends AmpObjectRepository<Action> {

	Action findFirstByName(String name);
	Action findFirstByActionTypeAndTargetType(ActionType actionType, TargetType targetType);
	Action findFirstByHttpMethodAndUrlPattern(HttpMethod httpMethod, String urlPattern);

	List<Action> findByActionTypeInAndTargetTypeIn(List<ActionType> actionTypes, List<TargetType> targetTypes);	
	List<Action> findByHttpMethodInAndUrlPatternIn(List<HttpMethod> httpMethods, List<String> urlPatterns);
	
}
