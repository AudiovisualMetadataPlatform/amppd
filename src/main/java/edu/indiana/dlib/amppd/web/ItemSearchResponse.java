package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import edu.indiana.dlib.amppd.model.dto.ItemFilesInfo;
import lombok.Data;

@Data
public class ItemSearchResponse {
	private boolean success = true;
	private String error = "";
	private List<ItemFilesInfo> items = new ArrayList<ItemFilesInfo>();	
}
