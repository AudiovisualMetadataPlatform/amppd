package edu.indiana.dlib.amppd.model.projection;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmScoringParameter;

/**
 * Projection for a brief view of an MgmScoringParameter.
 * @author rimshakhalid
 */
@Projection(name = "brief", types = {MgmScoringParameter.class})
public interface MgmScoringParameterBrief extends MgmMetaBrief {
    @Value("#{target.isRequired}")
    public Boolean getRequired();

    public String getType();
    public Double getMin();
    public Double getMax();
    public String getSelections();
    public String getDefaultValue();
    public String getShortName();    
    public String getUnit();

}
