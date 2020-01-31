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
 * Class containing information of a batch file. This corresponds to a row in a batch manifest spreadsheet.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class BatchFile {	
	public BatchFile() {
		batchSupplementFiles = new ArrayList<BatchSupplementFile>();
	}
	// in batch manifest the types are indicated as "C", "I", "P"
	public enum SupplementType { COLLECTION, ITEM, PRIMARYFILE };

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	int id;
	
	int rowNum;
	
	@JsonIgnore
	@ManyToOne
	Collection collection;
	String sourceIdType;
	String sourceId;
	String itemName;
	String itemDescription;
	String primaryfileFilename;
	String primaryfileName;
	String primaryfileDescription;
	SupplementType supplementType; 
	
	@OneToMany(mappedBy="batchFile")
	List<BatchSupplementFile> batchSupplementFiles;	
	
	@JsonIgnore
	@ManyToOne
	Batch batch;
	
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
