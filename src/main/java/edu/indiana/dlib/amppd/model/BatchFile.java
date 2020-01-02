package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

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
	// in batch manifest the types are indicated as "C", "I", "P"
	public enum SupplementType { COLLECTION, ITEM, PRIMARYFILE };

	Collection collection;
	String sourceIdType;
	String sourceId;
	String itemName;
	String itemDescription;
	String primaryfileFilename;
	String primaryfileName;
	String primaryfileDescription;
	SupplementType supplementType; 
	
	@OneToMany(mappedBy="batchFiles")
	List<BatchSupplementFile> batchSupplementFiles;	
	
	@ManyToOne
	Batch batch;
	
}
