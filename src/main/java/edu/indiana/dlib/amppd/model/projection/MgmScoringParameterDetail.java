package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmScoringParameter;

@Projection(name = "detail", types = {MgmScoringParameter.class})
public interface MgmScoringParameterDetail extends MgmScoringParameterBrief {
    @Value("#{target.mst.id}")
    public Long getMstId();

    @Value("#{target.mst.name}")
    public String getMstName();

    @Value("#{target.dependency?.name ?: ''}")
    public String getDependencyName();

    @Value("#{target.dependency?.id ?: ''}")
    public Long getDependencyId();
}
