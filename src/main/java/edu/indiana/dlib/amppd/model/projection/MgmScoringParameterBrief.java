package edu.indiana.dlib.amppd.model.projection;


import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection for a brief view of an MgmScoringParameter.
 * @author rimshakhalid
 */
@Projection(name = "brief", types = {MgmScoringParameter.class})
public interface MgmScoringParameterBrief extends MgmMetaBrief {
    @Value("#{target.mst.id}")
    public Long getMstId();

    @Value("#{target.dependency?.name ?: ''}")
    public String getDependencyName();
}
