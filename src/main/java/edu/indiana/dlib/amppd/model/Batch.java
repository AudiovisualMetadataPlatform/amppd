package edu.indiana.dlib.amppd.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Index;
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
 * Class containing information of a batch manifest. Fields that apply to the whole spreadsheet such as collection name are entered via UI; 
 * while fields of each batch file comes from each row in the manifest spreadsheet. A Batch could contain files from multiple collections.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Batch {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	// the following fields come from batch upload UI
	//@NotNull
	@Index
	@ManyToOne
	private AmpUser submitUser;

	//@NotNull
	@Index
	private Date submitTime;

	//@NotNull
	@Index
	private String manifestFilename;
	
	//@NotNull
	@Index
	@JsonIgnore
	@ManyToOne
	private Unit unit;
	
	// the following info come from batch manifest
	@OneToMany(mappedBy="batch")
	private List<BatchFile> batchFiles;

	public void addBatchFile(BatchFile batchFile) {
		if(batchFiles==null) batchFiles = new ArrayList<BatchFile>();
		batchFiles.add(batchFile);
	}

	public boolean isDuplicatePrimaryfileFilename(String filename, int rowNum) {
		for(BatchFile row : batchFiles) {
			if(row.getRowNum()==rowNum) continue;
			if(row.getPrimaryfileFilename().equals(filename)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isDuplicatePrimaryfileName(String name, String itemExternalSourceId, String itemName, int rowNum) {
		for(BatchFile row : batchFiles) {
			if(row.getRowNum()==rowNum) continue;

			// Primaryfile names need to be unique within an item
			// Uniqueness of items is determined by source ID if provided, OR item name when source ID is not provided.
			boolean primaryfileNameMatches = row.getPrimaryfileName().equals(name);
			boolean itemSourceIdMatches = !itemExternalSourceId.isEmpty() && !row.getExternalId().isEmpty() && row.getExternalId().equals(itemExternalSourceId);
			boolean itemNameMatches = (itemExternalSourceId.isEmpty() || row.getExternalId().isEmpty()) && row.getItemName().equals(itemName);
			
			if(primaryfileNameMatches && (itemSourceIdMatches || itemNameMatches)) {
				return true;
			}
		}
		return false;
	}
}
