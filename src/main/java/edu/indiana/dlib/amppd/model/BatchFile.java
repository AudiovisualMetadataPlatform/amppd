package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Class containing information of a batch file. This corresponds to a row in a batch manifest spreadsheet.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class BatchFile {	
	public enum FileType { PRIMARYFILE, COLLECTION_SUPPLEMENT, ITEM_SUPPLEMENT, PRIMARYFILE_SUPPLEMENT };

	String sourceIdType;
	String sourceId;
	String itemName;
	String itemDescription;
	FileType fileType; 
	String primaryfileFilename;
	String primaryfileName;
	String primaryfileDescription;
	String supplementFilename;	
	
	@ManyToOne
	Batch batch;
	
}
