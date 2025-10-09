package edu.indiana.dlib.amppd.web;

import java.util.Arrays;
import java.util.List;

public enum GalaxyJobState {
	UNKNOWN,		// 0
	SCHEDULED,		// 1
	IN_PROGRESS,	// 2
	PAUSED,			// 3
	COMPLETE,		// 4
	ERROR,			// 5
	DELETED;		// 6
	

	public static final List<GalaxyJobState> INCOMPLETE_STATUSES = Arrays.asList(
			SCHEDULED,
			IN_PROGRESS,
			PAUSED
	);
	public static final List<GalaxyJobState> RUNNING_STATUSES = Arrays.asList(
			SCHEDULED,
			IN_PROGRESS
	);
	
	/**
	 *  Map the given Galaxy job state to AMP representation as shown on the front end.
	 */
	public static GalaxyJobState getJobState(String state) {
		GalaxyJobState status = GalaxyJobState.UNKNOWN;
		if(state.equalsIgnoreCase("new") || state.equalsIgnoreCase("scheduled") || state.equalsIgnoreCase("queued")) {
			status = GalaxyJobState.SCHEDULED;
		}
		else if(state.equalsIgnoreCase("running")) {
			status = GalaxyJobState.IN_PROGRESS;
		}
		else if(state.equalsIgnoreCase("ok") || state.equalsIgnoreCase("complete") || state.equalsIgnoreCase("done")) {
			status = GalaxyJobState.COMPLETE;
		}
		else if(state.equalsIgnoreCase("error") || state.equalsIgnoreCase("failed")) {
			status = GalaxyJobState.ERROR;
		}
		else if(state.equalsIgnoreCase("paused")) {
			status = GalaxyJobState.PAUSED;
		}
		else if(state.equalsIgnoreCase("deleted") || state.equalsIgnoreCase("discarded") || state.equalsIgnoreCase("cancelled")) {
			status = GalaxyJobState.DELETED;
		}
		return status;
	}
	
}
