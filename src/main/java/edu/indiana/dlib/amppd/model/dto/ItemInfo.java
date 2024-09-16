package edu.indiana.dlib.amppd.model.dto;

import edu.indiana.dlib.amppd.model.projection.ContentBrief;

/**
 * Interface for ItemBrief fields without referring to parent chain objects.
 * @author yingfeng
 */
public interface ItemInfo extends ContentBrief {	
	public String getCollectionName();	
	public String getUnitName();
	public Long getCollectionId();	
	public Long getUnitId();		
}
