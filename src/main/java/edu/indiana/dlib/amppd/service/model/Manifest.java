package edu.indiana.dlib.amppd.service.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Manifest {
	public Manifest() {
		rows = new ArrayList<ManifestRow>();
	}
	String unitName;
	
	List<ManifestRow> rows;
	
	public void addRow(ManifestRow row) {
		rows.add(row);
	}
	
	
	public boolean isDuplicatePrimaryfileFilename(String fileName, int rowNum) {
		int c = 0;
		for(ManifestRow row : rows) {
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
		for(ManifestRow row : rows) {
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
