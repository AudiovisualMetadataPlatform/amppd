package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.Map;

import lombok.Data;

@Data
public class ItemSearchResult {
	private Long collectionId;
	private String collectionName;
	private Long itemId;
	private String itemName;
	private String externalSource;
	private String externalId;
	private ArrayList<Map> primaryfiles;
}
