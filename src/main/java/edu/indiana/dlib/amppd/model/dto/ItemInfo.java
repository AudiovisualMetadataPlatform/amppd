package edu.indiana.dlib.amppd.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class ItemInfo {
	private Long collectionId;
	private String collectionName;
	private Long itemId;
	private String itemName;
	private String externalSource;
	private String externalId;
	private List<PrimaryfileInfo> primaryfiles;
}
