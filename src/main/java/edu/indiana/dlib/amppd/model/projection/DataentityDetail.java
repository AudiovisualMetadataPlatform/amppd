package edu.indiana.dlib.amppd.model.projection;

import java.util.Date;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Dataentity;

/**
 * Projection for a detailed view of all data entities.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Dataentity.class}) 
public interface DataentityDetail extends DataentityBrief {

	public Long getId();
    public Date getCreatedDate();
    public Date getModifiedDate();    
    public String getCreatedBy();
    public String getModifiedBy();    

}
