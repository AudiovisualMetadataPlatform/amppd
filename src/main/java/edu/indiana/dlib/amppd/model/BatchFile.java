package edu.indiana.dlib.amppd.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * Class containing information of a batch file, which corresponds to a row in a batch manifest spreadsheet. 
 * It could be one of the following:
 * - a primaryfile plus its associated supplements if exists;
 * - an item Supplement or a collection supplement.   
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class BatchFile {	
	// in batch manifest the types are indicated as "C", "I", "P"
	public enum SupplementType { COLLECTION, ITEM, PRIMARYFILE };

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;	
	private int rowNum;	
	
	@JsonIgnore
	@ManyToOne
	private Collection collection;
	private String sourceIdType;
	private String sourceId;
	private String itemName;
	private String itemDescription;
	private String primaryfileFilename;
	private String primaryfileName;
	private String primaryfileDescription;
	private SupplementType supplementType; 
	
	@OneToMany(mappedBy="batchFile")
	private List<BatchSupplementFile> batchSupplementFiles;	
	
	@JsonIgnore
	@ManyToOne
	private Batch batch;
	
	public BatchFile() {
		batchSupplementFiles = new ArrayList<BatchSupplementFile>();
	}

	public void addSupplement(BatchSupplementFile batchSupplement) {
		if(batchSupplementFiles==null) batchSupplementFiles = new ArrayList<BatchSupplementFile>();
		
		batchSupplementFiles.add(batchSupplement);
	}
	public boolean containsSupplementFilename(String name, int rowNum, int supplementNum) {
		for(BatchSupplementFile supplement : batchSupplementFiles) {
			if(this.rowNum==rowNum && supplementNum==supplement.getSupplementNum()) continue;
			if(supplement.getSupplementFilename().equals(name)) {
				return true;
			}
		}
		return false;
	}
	public boolean containsSupplementName(String name, int rowNum, int supplementNum) {
		for(BatchSupplementFile supplement : batchSupplementFiles) {
			if(this.rowNum==rowNum && supplementNum==supplement.getSupplementNum()) continue;
			if(supplement.getSupplementName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	
}
