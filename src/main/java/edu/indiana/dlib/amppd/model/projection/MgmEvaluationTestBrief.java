package edu.indiana.dlib.amppd.model.projection;

import java.util.Date;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;

@Projection(name = "brief", types = {MgmEvaluationTest.class})
public interface MgmEvaluationTestBrief {
	
    public Long getId();
    public String getStatus();
    public Date getDateSubmitted();
    public String getSubmitter();
    
}
