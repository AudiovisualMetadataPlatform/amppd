package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.List;

import edu.indiana.dlib.amppd.model.WorkflowResult;
import lombok.Data;

@Data
public class ItemSearchResponse {
	private boolean success;
	private String error;
	private List<ItemSearchResult> rows;
}
