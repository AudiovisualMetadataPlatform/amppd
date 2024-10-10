package edu.indiana.dlib.amppd.handler;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PostLoad;

import org.springframework.beans.factory.annotation.Autowired;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.web.GalaxyJobState;

/**
 * EntityListener to handle extra Dataentity life cycle events that can't be handled by Spring Data JPA events.
 */
public class DataentityListener {
	
	// incomplete GalaxyJobStates which would cause corresponding primaryfiles and their parent chain not deletable
	public static final List<GalaxyJobState> INCOMPLETE_STATUSES = Arrays.asList(
			GalaxyJobState.SCHEDULED,
			GalaxyJobState.IN_PROGRESS,
			GalaxyJobState.PAUSED
	);
	
	@Autowired
	private WorkflowResultRepository workflowResultRepository;
	
	@PostLoad
	private void afterLoad(Primaryfile primaryfile) {
		Boolean deletable = !workflowResultRepository.existsByPrimaryfileIdAndStatusIn(primaryfile.getId(), INCOMPLETE_STATUSES);
		primaryfile.setDeletable(deletable);   
	}
	
}
