package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.Map;

import lombok.Data;

@Data
public class ItemSearchResult {
	private String itemName;
	//private ArrayList<String> primaryFileNames;
	private ArrayList<Map> primaryFiles;

}
