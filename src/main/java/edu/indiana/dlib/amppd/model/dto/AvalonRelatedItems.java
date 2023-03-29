package edu.indiana.dlib.amppd.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Class containing fields corresponding to the related items of an Avalon media object (which corresponds to an AMP item). 
 * These information come from the corresponding AMP item's primaryfiles and the BagContents contained.
 * @author yingfeng
 */
@Data
@AllArgsConstructor
public class AvalonRelatedItems {
	private String collection_id;					// collection id of the Avalon media object
	private AvalonRelatedItemsFields fields; 		// fields to be updated in Avalon media object
	
	@Data
	@AllArgsConstructor
	public static class AvalonRelatedItemsFields {
		private List<String> related_item_url;		// related_item_url array in Avalon media object
		private List<String> related_item_label;	// related_item_label array in Avalon media object		
	}
}
