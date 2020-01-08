package edu.indiana.dlib.amppd.service.model;

import java.util.ArrayList;
import java.util.List;

import edu.indiana.dlib.amppd.model.BatchFile.SupplementType;
import lombok.Data;

@Data
public class ManifestRow {
	public ManifestRow() {
		supplements = new ArrayList<ManifestSupplement>();
	}
	int rowNum;
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
	
	public void addSupplement(ManifestSupplement supplement) {
		supplements.add(supplement);
	}
	public boolean containsSupplementFilename(String name, int rowNum, int supplementNum) {
		for(ManifestSupplement supplement : supplements) {
			if(this.rowNum==rowNum && supplementNum==supplement.getSupplementNum()) continue;
			if(supplement.getFilename().equals(name)) {
				return true;
			}
		}
		return false;
	}
	public boolean containsSupplementName(String name, int rowNum, int supplementNum) {
		for(ManifestSupplement supplement : supplements) {
			if(this.rowNum==rowNum && supplementNum==supplement.getSupplementNum()) continue;
			if(supplement.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
