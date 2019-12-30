package edu.indiana.dlib.amppd.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Class containing information of a batch manifest. Fields that apply to the whole spreadsheet such as collection name are entered via UI; 
 * while fields of each batch file comes from each row in the manifest spreadsheet.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Batch {
	AmpUser submitUser;
	Date submitTime;
	String manifestFilename;
	Collection collection;
	
	@OneToMany(mappedBy="batch")
	List<BatchFile> batchFiles;
	
}
