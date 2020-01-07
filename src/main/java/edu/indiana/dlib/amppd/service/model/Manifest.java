package edu.indiana.dlib.amppd.service.model;

import java.util.List;

import edu.indiana.dlib.amppd.model.BatchFile.SupplementType;
import lombok.Data;

@Data
public class Manifest {
	String unitName;
	String collectionName;
	String sourceIdType;
	String sourceId;
	String itemName;
	String itemDescription;
	String primaryfileFilename;
	String primaryfileName;
	String primaryfileDescription;
	SupplementType supplementType; 
	List<ManifestSupplement> supplements;
}
