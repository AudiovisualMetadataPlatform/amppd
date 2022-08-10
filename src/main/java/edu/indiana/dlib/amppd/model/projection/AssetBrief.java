package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Asset;

/**
 * Projection for a brief view of an asset.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Asset.class}) 
public interface AssetBrief extends DataentityBrief {

	public String getOriginalFilename();

}
