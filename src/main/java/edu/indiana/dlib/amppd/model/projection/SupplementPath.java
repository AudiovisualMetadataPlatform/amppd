package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Primaryfile;

/**
 * Projection for a detailed view of an item.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Primaryfile.class}) 
public interface SupplementPath extends SupplementDetail {

	public String getAbsolutePathname();	

}
