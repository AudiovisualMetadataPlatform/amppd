package edu.indiana.dlib.amppd.web;

import lombok.Data;
import java.util.Date;

@Data
public class MgmEvaluationTestResult {
    private Long id;
    private Date testDate;
    private Date OutputDate;
    private String submitter;
    private String unit;
    private String collection;
    private String externalSource;
    private String externalId;
    private String item;
    private String primaryFile;
    private String workflow;
    private String step;
    private String output;
    private String groundTruth;
    private String testOutput;
    private String workflowId;
}
