package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class SaveNerRequest {
	private String json;
	private String filePath;
}
