package edu.indiana.dlib.amppd.web;

import lombok.Data;
import java.util.Date;

@Data
public class MgmEvaluationTestResult {
    private Long id;
    private Date testDate;
    private Date outputDate;
    private String submitter;
    private String unitName;
    private String collectionName;
    private String externalSource;
    private String externalId;
    private String itemName;
    private String primaryfileName;
    private String workflowName;
    private String workflowStep;
    private String outputName;
    private String outputLabel;
    private String groundTruth;
    private String outputTest;
    private String workflowId;
}
