package edu.indiana.dlib.amppd.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Class for Primaryfile brief DTO.
 * @author yingfeng
 */
@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class PrimaryfileInfo {
	private Long id;
	private String name;
	private String mimeType;
	private String originalFilename;
}
