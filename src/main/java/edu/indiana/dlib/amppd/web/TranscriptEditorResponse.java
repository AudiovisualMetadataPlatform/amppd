package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class TranscriptEditorResponse {
	String content;
	boolean isTemporaryFile;
	boolean success;
	boolean complete;
}
