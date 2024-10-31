package edu.indiana.dlib.amppd.model;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Index;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
@Index(members={"externalSource","externalId"})
@Data
public class BatchFile {	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;	

	//@NotNull
	@Index
	private Integer rowNum;	
	
	@Index
	@JsonIgnore
	@ManyToOne
	private Collection collection;
    
	//@NotNull
	@Index
	@Type(type="text")
	private String collectionName;
    
	@Type(type="text")
	private String externalSource;
	
	@Type(type="text")
	private String externalId;
	
	@Index
	@Type(type="text")
	private String itemName;

	@Type(type="text")
	private String itemDescription;
	
	private String primaryfileFilename;

	@Index
	@Type(type="text")
	private String primaryfileName;

	@Type(type="text")
	private String primaryfileDescription;

	@Index
	private SupplementType supplementType; 
	
	@OneToMany(mappedBy="batchFile", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private List<BatchSupplementFile> batchSupplementFiles;	
	
	//@NotNull
	@Index
	@JsonBackReference(value="batch")
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