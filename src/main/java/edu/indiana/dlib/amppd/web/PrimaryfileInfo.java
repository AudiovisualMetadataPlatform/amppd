package edu.indiana.dlib.amppd.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class PrimaryfileInfo {
	private Long id;
	private String name;
	private String mimeType;
	private String originalFilename;
}
