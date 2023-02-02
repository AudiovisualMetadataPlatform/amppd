package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ItemSearchResponse {
	private boolean success = true;
	private String error = null;
	private List<ItemInfo> rows = new ArrayList<ItemInfo>();	
}
