package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;


/**
 * Projection for a detailed view of an item.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Primaryfile.class}) 
public interface PrimaryfileDetail extends AssetDetail {

	public String getHistoryId();	
	public Set<PrimaryfileSupplement> getSupplements();

}
