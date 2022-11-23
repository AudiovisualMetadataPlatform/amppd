package edu.indiana.dlib.amppd.model.projection;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "detail", types = {MgmEvaluationTest.class})
public interface MgmEvaluationTestDetail extends MgmEvaluationTestBrief {
    @Value("#{target.groundtruthSupplement.name}")
    public String getGroundtruthFilename();
    public String getSubmitter();
    @Value("#{target.groundtruthSupplement.primaryfile.name}")
    public String getPrimaryFilename();
    @Value("#{target.category.name}")
    public String getCategoryName();
    @Value("#{target.mst.name}")
    public String getMstName();
    public WorkflowResult getWorkflowResult();
    public String getScorePath();
    public String getScores();
    public String getParameters();
}
