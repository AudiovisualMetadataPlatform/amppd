package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;

/**
 * Class containing information of a batch supplement file. 
 * This corresponds to a 3-column tuple specifying info for a unit/collection/primaryfile supplement.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class BatchSupplementFile {

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private int supplementNum;
	private String supplementFilename;	
	
    @Type(type="text")
	private String supplementName;
    
    @Type(type="text")
	private String supplementDescription;

	@JsonBackReference(value="batch-file")
	@ManyToOne
	private BatchFile batchFile;
	
}

