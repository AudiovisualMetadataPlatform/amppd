package edu.indiana.dlib.amppd.web;

import java.util.List;

import lombok.Data;

/**
 * Class containing fields corresponding to the related items of an Avalon media object (which corresponds to an AMP item). 
 * These information come from the corresponding AMP item's primaryfiles and the BagContents contained.
 * while 
 * @author yingfeng
 */
@Data
public class AvalonRelatedItems {
//	private Long resultId;	// the resultId of the corresponding BagContent 
	private List<String> urls;		// related_item_url array in Avalon media object
	private List<String> labels;	// related_item_label array in Avalon media object
}
