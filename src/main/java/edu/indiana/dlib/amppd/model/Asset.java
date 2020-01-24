package edu.indiana.dlib.amppd.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Asset represents a file containing either media content of any MIME type or annotation of a media file in pdf/text/json format, 
 * which can be the input/output of a workflow or MGM. 
 * @author yingfeng
 *
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@TypeDef(
	    name = "jsonb",
	    typeClass = JsonBinaryType.class
	)
public abstract class Asset extends Content {

	private String originalFilename;	// the file name of the original file uploaded by user or batch
    private String pathname;			// path name relative to storage root for the file associated with the asset
    private String datasetId;			// ID of the dataset as a result of upload to Galaxy
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String mediainfo;			// technical media information extracted from the asset file, to be stored as a JSON blob 
    
}
