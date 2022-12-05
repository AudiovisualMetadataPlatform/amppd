package edu.indiana.dlib.amppd.model.projection;


import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection for a brief view of an MgmScoringParameter.
 * @author rimshakhalid
 */
@Projection(name = "brief", types = {MgmScoringParameter.class})
public interface MgmScoringParameterBrief extends MgmMetaBrief {
    public String getType();
    public Double getMin();
    public Double getMax();

    public String getSelections();

    public Boolean getRequired();

    public String getDefault_value();
    public String getUnit();
    public String getShort_name();
}
