package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.WorkflowResult;

@Projection(name = "detail", types = {MgmEvaluationTest.class})
public interface MgmEvaluationTestDetail extends MgmEvaluationTestBrief {
    @Value("#{target.category.name}")
    public String getCategoryName();
    
    @Value("#{target.mst.name}")
    public String getMstName();
    
    @Value("#{target.groundtruthSupplement.name}")
    public String getGroundtruthName();
    
    @Value("#{target.groundtruthSupplement.primaryfile.name}")
    public String getPrimaryfileName();
    
    @Value("#{target.groundtruthSupplement.primaryFile.originalFilename}")
    public String getPrimaryfileMedia();

    public WorkflowResult getWorkflowResult();
    public String getScorePath();
    public String getScores();
    public String getParameters();

}
