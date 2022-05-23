package edu.indiana.dlib.amppd.model.projection;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Asset;


/**
 * Projection for a detailed view of an asset.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Asset.class}) 
public interface AssetDetail extends DataentityDetail {

	public String getOriginalFilename();
    public String getPathname();
	public String getDatasetId();
    public String getSymlink();
    public String getMediaInfo();	
    public String getMimeType();	

}