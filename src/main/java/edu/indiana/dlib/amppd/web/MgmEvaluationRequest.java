package edu.indiana.dlib.amppd.web;

import lombok.Data;

import java.util.ArrayList;

@Data
public class MgmEvaluationRequest {
    private Long categoryId;
    private Long mstId;
    private ArrayList<MgmEvaluationParameterObj> parameters;
    private ArrayList<MgmEvaluationFilesObj> files;
    
    @Override
    public String toString() {
    	String str = "MgmEvaluationRequest<";
    	str += " categoryId: " + categoryId;
    	str += ", mstId: " + mstId;
    	str += ", # of parameters: " + parameters.size();
    	str += ", # of WorkflowResult-Groundtruth pairs: " + files.size();
		str += " >";
		return str;
    }
    
}
