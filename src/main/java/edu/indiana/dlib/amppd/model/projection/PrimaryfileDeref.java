package edu.indiana.dlib.amppd.model.projection;

/**
 * Projection for PrimaryfileBrief fields without referring to parent chain objects.
 * @author yingfeng
 */
public interface PrimaryfileDeref extends ItemDeref, AssetDetail {	
	public String getItemName();	
	public Long getItemId();		
}
