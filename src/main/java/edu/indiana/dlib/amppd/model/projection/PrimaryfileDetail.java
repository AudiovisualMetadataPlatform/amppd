package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;


/**
 * Projection for a detailed view of a primaryfile.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Primaryfile.class}) 
public interface PrimaryfileDetail extends PrimaryfileBrief, AssetDetail {

	public String getHistoryId();	
	public String getDatasetId();
	public Set<PrimaryfileSupplement> getSupplements();

}
