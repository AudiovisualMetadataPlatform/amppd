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
    public String getGroundtruthSubcategory(); 
    public String getGroundtruthFormat(); 
	public String getWorkflowResultOutput();
	
	@Value("#{target.dependencyParameter == null ? null : target.dependencyParameter.name}")
	public String getDependencyParamName();

	@Value("#{target.category.id}")
	public String getCategoryId();

	public Set<MgmScoringParameter> getParameters();
	
}
