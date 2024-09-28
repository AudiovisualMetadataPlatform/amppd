package edu.indiana.dlib.amppd.model.projection;

/**
 * Projection for ItemBrief fields without referring to parent chain objects.
 * @author yingfeng
 */
public interface ItemDeref extends ContentBrief {	
	public String getCollectionName();	
	public String getUnitName();
	public Long getCollectionId();	
	public Long getUnitId();		
}
