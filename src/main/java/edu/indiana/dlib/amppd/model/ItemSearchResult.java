package edu.indiana.dlib.amppd.model;

import java.util.ArrayList;
import java.util.Map;

import lombok.Data;

@Data
public class ItemSearchResult {
	private String itemName;
	//private ArrayList<String> primaryFileNames;
	private Map<String, String> primaryFiles;

}
