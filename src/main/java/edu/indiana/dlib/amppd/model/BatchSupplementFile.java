package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Class containing information of a batch supplement file. This corresponds to a 3-column tuple specifying information for a unit/collection/primaryfile supplement.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class BatchSupplementFile {

	String supplementFilename;	
	String supplementName;
	String supplementDescription;
	
	@ManyToOne
	BatchFile batchFile;
	
}

