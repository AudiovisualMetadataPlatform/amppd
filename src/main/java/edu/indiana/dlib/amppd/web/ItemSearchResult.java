package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.Map;

import lombok.Data;

@Data
public class ItemSearchResult {
	private String collectionName;
	private String itemName;
	private String externalSource;
	private String externalId;
	private ArrayList<Map> primaryfiles;

}
