package edu.indiana.dlib.amppd.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Class containing information of a batch manifest. 
 * Fields that apply to the whole spreadsheet such as unit name are entered via UI; while fields related to batch files comes from each row in the manifest.
 * It's assumed that unit/collections referred in the manifest preexist, and the media files also exist in their corresponding unit/collection subfolders in the dropbox. 
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Batch {
	// the following fields come from batch upload UI
	AmpUser submitUser;
	Date submitTime;
	String manifestFilename;
	Unit unit;
	
	// the following info come from batch manifest
	@OneToMany(mappedBy="batch")
	List<BatchFile> batchFiles;
	
}
