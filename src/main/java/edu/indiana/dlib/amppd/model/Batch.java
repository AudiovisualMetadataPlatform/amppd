package edu.indiana.dlib.amppd.model;

import java.util.ArrayList;
import java.util.Date;
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
	@ManyToOne
	AmpUser submitUser;
	Date submitTime;
	String manifestFilename;
	
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

	public boolean isDuplicatePrimaryfileFilename(String fileName, int rowNum) {
		int c = 0;
		for(BatchFile row : batchFiles) {
			if(row.getRowNum()==rowNum) continue;
			if(row.getPrimaryfileFilename().equals(fileName)) {
				c++;
				if(c>1) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isDuplicatePrimaryfileName(String name, int rowNum) {
		int c = 0;
		for(BatchFile row : batchFiles) {
			if(row.getRowNum()==rowNum) continue;
			if(row.getPrimaryfileName().equals(name)) {
				c++;
				if(c>1) {
					return true;
				}
			}
		}
		return false;
	}
}
