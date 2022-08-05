package edu.indiana.dlib.amppd.model.projection;

import java.util.Date;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.AmpObject;

/**
 * Projection for a detailed view of an AMP object, suitable for displaying individual object.
 * @author yingfeng
 */
@Projection(name = "detail", types = {AmpObject.class}) 
public interface AmpObjectDetail extends AmpObjectBrief {
	
    public Date getCreatedDate();
    public Date getModifiedDate();    
    public String getCreatedBy();
    public String getModifiedBy();
    
}
