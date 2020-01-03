package edu.indiana.dlib.amppd.model;

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
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	int id;
	
	// the following fields come from batch upload UI
	@ManyToOne
	AmpUser submitUser;
	Date submitTime;
	String manifestFilename;
	@ManyToOne
	Unit unit;
	
	// the following info come from batch manifest
	@OneToMany(mappedBy="batch")
	List<BatchFile> batchFiles;
	
}
