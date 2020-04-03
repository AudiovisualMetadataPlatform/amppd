package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class NerEditorResponse {
	String content;
	boolean isTemporaryFile;
	boolean success;
	boolean complete;
}
