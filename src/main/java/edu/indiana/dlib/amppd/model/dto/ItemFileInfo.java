package edu.indiana.dlib.amppd.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Class for Item/File info.
 * @author yingfeng
 */
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class ItemFileInfo {
	private Long collectionId;
	private String collectionName;
	private Long itemId;
	private String itemName;
	private String externalSource;
	private String externalId;
	private List<PrimaryfileInfo> primaryfiles;
}
