package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ItemSearchResponse {
	private boolean success = true;
	private String error = "";
	private List<ItemInfo> items = new ArrayList<ItemInfo>();	
}
