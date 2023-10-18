package edu.indiana.dlib.amppd.web;

import java.util.Date;

import lombok.Data;

@Data
public class MgmEvaluationTestResult {
    // MgmEvaluationTest fields
	private Long id;				// unique per MgmEvaluationTestResult
    private Date testDate;
    private String testSubmitter;
    private String groundTruth;
    private String scores;

    // WorkflowResult fields
    private Long workflowResultId;	// unique per WorkflowResult, but non-unique per MgmEvaluationTestResult
    private Date dateCreated;
    private String submitter;
    private Long primaryfileId;
    private Long itemId;
    private Long collectionId;
    private Long unitId;
    private String primaryfileName;
    private String itemName;
    private String collectionName;
    private String unitName;
    private String externalSource;
    private String externalId;
    private String workflowId;
    private String workflowName;
    private String workflowStep;
    private String outputId;
    private String outputName;
    private String outputLabel;    
    
    // Note: some WorkflowResult fields such as status and outputPath are not included, 
    // as the UI doesn't need them, and the status would be COMPLETE in this case.
}
