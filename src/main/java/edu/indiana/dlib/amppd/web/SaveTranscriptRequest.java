package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class SaveTranscriptRequest {
	private String json;
	private String filePath;
}
