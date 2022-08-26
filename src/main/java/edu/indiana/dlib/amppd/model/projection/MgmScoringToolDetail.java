package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;

/**
 * Projection for a detailed view of an MgmScoringTool.
 * @author yingfeng
 */
@Projection(name = "detail", types = {MgmScoringTool.class}) 
public interface MgmScoringToolDetail extends MgmScoringToolBrief, MgmMetaDetail {
	
	public String getScriptPath(); 
    public String getWorkflowResultType(); 
    public String getGroundtruthFormat(); 
	public Set<MgmScoringParameter> getParameters();
	
	@Value("#{target.category.id}")
	public String getCategoryId();

}
