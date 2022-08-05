package edu.indiana.dlib.amppd.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Asset represents a file containing either media content of any MIME type or annotation of a media file in pdf/text/json format, 
 * which can be the input/output of a workflow or MGM. 
 * @author yingfeng
 *
 */
@MappedSuperclass
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Slf4j
public abstract class Asset extends Dataentity {

	private String originalFilename;	// the file name of the original file uploaded by user or batch	
    private String pathname;			// path name relative to storage root for the file associated with the asset
    private String symlink;				// the symlink under the static content directory used for serving large media file
    
    // Note: mediaInfo must be a valid json string
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String mediaInfo;			// technical media information extracted from the asset file, to be stored as a JSON blob 
     
    // only used for media file upload during asset creation/update, so it can be sent as part of RequestBody
    @Transient
    private MultipartFile mediaFile;

    // a workaround to allow absolute path being set by services outside of Asset 
    // as below code using #Value amppd.fileStorageRoot doesn't work and root is always null
    @Transient
    private String absolutePathname;

//    @Transient
//    @Value("${amppd.fileStorageRoot}")
//    private String root;
    
//    /**
//     * Get the absolute pathname of the asset.
//     */
//    public String getAbsolutePathname() {
//    	return Paths.get(root, pathname).toString();
//    }
    
    /**
     * Get the MIME type of the asset based on the MIME and streams from its media info.
     */
	public String getMimeType() {		
		// media info should never be null; otherwise pre-process must have failed and the asset wasn't ingested/uploaded properly
		if (mediaInfo == null) {
			log.error("Empty media info JSON for asset " + getId());		
			return null;
		}
		
		try {
			JSONObject jsonObject = new JSONObject(mediaInfo);
			JSONObject container = jsonObject.getJSONObject("container");
			JSONObject streams = jsonObject.getJSONObject("streams");
			String mime = container.getString("mime_type");
			
			// in case the mp4 file contains only audio but no video, correct the MIME type from video to audio
			if (StringUtils.contains(mime, "video") && !streams.has("video") && streams.has("audio")) {
				mime = StringUtils.replace(mime,  "video", "audio");
			}
			
			// otherwise return the MIME type as identified by ffmpeg during preprocessing
			return mime;
		} catch (JSONException e) {
			log.error("Invalid media info JSON for asset " + getId());
			return null;
		}
	}
	
//    /**
//     * Override just to change the groups for name validations.
//     */
//    @Override
//	@NotBlank(groups = {WithReference.class, WithoutReference.class})
//    public String getName() {
//    	return super.getName();
//    }
    
}
