package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Content;

/**
 * Projection for a brief view of a content.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Content.class}) 
public interface ContentBrief extends DataentityBrief {

    public String getExternalSource();
    public String getExternalId();

}
