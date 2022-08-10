package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Content;


/**
 * Projection for a detailed view of a content.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Content.class}) 
public interface ContentDetail extends ContentBrief, DataentityDetail {

}