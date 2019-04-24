package edu.indiana.dlib.amppd.model;

import javax.persistence.MappedSuperclass;

import org.json.JSONObject;

import lombok.Data;

/**
 * Asset represents a file containing either media content of any MIME type or annotation of a media file in pdf/text/json format, 
 * which can be the input/output of a workflow or MGM. 
 * @author yingfeng
 *
 */
@MappedSuperclass
@Data
public abstract class Asset extends Content {

	private String originalFileName;	// the file name of the original file uploaded by user or batch
    private String pathName;			// path name relative to storage root for the file associated with the asset
    private String metainfo;			// technical meta data information extracted/associated from the asset file, to be stored as a JSON blob 
//    private JSONObject metainfo;		// TODO: investigate how we can use JSONObject here
    
}
