package edu.indiana.dlib.amppd.model;

import javax.persistence.ManyToOne;

/**
 * Class containing information of a batch supplement file. This corresponds to a 3-column tuple specifying info for a unit/collection/primaryfile supplement.
 * @author yingfeng
 *
 */
public class BatchSupplementFile {

	String supplementFilename;	
	String supplementName;
	String supplementDescription;
	
	@ManyToOne
	BatchFile batchFile;
	
}

