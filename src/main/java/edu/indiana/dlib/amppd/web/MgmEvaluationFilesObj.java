package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class MgmEvaluationFilesObj {
    private MgmEvaluationGroundtruthObj groundtruthFile;
    private MgmEvaluationPrimaryFileObj primaryFile;
    private Long workflowId;
}
