package edu.indiana.dlib.amppd.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.web.multipart.MultipartFile;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import edu.indiana.dlib.amppd.validator.WithReference;
import edu.indiana.dlib.amppd.validator.WithoutReference;
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
@EqualsAndHashCode(callSuper=true)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@ToString(callSuper=true)
public abstract class Asset extends Dataentity {

	private String originalFilename;	// the file name of the original file uploaded by user or batch	
    private String pathname;			// path name relative to storage root for the file associated with the asset

	private String datasetId;			// ID of the dataset as a result of upload to Galaxy
    private String symlink;				// the symlink under the static content directory used for serving large media file
    
    // Note: mediaInfo must be a valid json string
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String mediaInfo;			// technical media information extracted from the asset file, to be stored as a JSON blob 
     
    // only used for media file upload during asset creation/update, so it can be sent as part of RequestBody
    @Transient
    MultipartFile mediaFile;
    
//    /**
//     * Override just to change the groups for name validations.
//     */
//    @Override
//	@NotBlank(groups = {WithReference.class, WithoutReference.class})
//    public String getName() {
//    	return super.getName();
//    }
    
}
