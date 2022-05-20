package edu.indiana.dlib.amppd.web;

import java.util.List;

import lombok.Data;

@Data
public class MgmEvaluationResponse {
	
	private List<MgmEvaluationCategory> CategoryList;
	private List<MgmEvaluationTool> toolList;
	private List<MgmEvaluationScoringTool> scoringList;

}
