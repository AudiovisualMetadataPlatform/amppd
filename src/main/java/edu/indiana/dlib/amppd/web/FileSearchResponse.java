package edu.indiana.dlib.amppd.web;

import java.util.List;

import edu.indiana.dlib.amppd.model.DashboardResult;
import lombok.Data;

@Data
public class FileSearchResponse {
	private List<DashboardResult> rows;
	String keyword;
}
