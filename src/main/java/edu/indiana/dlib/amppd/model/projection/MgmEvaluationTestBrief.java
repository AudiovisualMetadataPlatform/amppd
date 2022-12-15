package edu.indiana.dlib.amppd.model.projection;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import org.springframework.data.rest.core.config.Projection;
import java.util.Date;

@Projection(name = "brief", types = {MgmEvaluationTest.class})
public interface MgmEvaluationTestBrief {
    public Long getId();
    public String getStatus();
    public Date getTestDate();
}
