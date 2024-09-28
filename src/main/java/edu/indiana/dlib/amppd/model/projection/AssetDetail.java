package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Asset;


/**
 * Projection for a detailed view of an asset.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Asset.class}) 
public interface AssetDetail extends AssetBrief, DataentityDetail {

    public String getPathname();
    public String getSymlink();
    public String getMediaInfo();	
    
    // below 2 transient fields won't be mapped by Spring Data and will return null from DB queries
//    public String getAbsolutePathname();	
//    public String getMimeType();	

}