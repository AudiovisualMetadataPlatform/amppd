package edu.indiana.dlib.amppd.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import edu.indiana.dlib.amppd.model.BatchFile.SupplementType;
import edu.indiana.dlib.amppd.service.ManifestValidationService;
import edu.indiana.dlib.amppd.web.ValidationResponse;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;

@Service
public class ManifestValidationServiceImpl implements ManifestValidationService {
	String dropBoxBase = "/srv/amp/dropbox";
	

	@Autowired
    private UnitRepository unitRepository;
	
	@Autowired
    private CollectionRepository collectionRepository;
	
	@Autowired
    private PrimaryfileRepository primaryFileRepository;

	@Autowired
    private PrimaryfileSupplementRepository primaryFileSupplementRepository;
	
	@Autowired
    private CollectionSupplementRepository collectionSupplementRepository;

	@Autowired
    private ItemSupplementRepository itemSupplementRepository;
	
	public ValidationResponse validate(String unitName, String fileContent) {

		ValidationResponse response = new ValidationResponse();
		
		List<String[]> lines = parse(fileContent);
		
		if(lines.size()<=1) {
			response.addError("Invalid file. No rows supplied.");
			return response;
		}
		
		// Validate supplied unit name
		List<String> unitErrors = validateUnit(unitName);
    	response.addErrors(unitErrors);
		
    	// If we have an invalid unit, no point on continuing with validation
		if(unitErrors.size()>0) {
			return response;
		}
		
        for(int lineNum = 1; lineNum < lines.size(); lineNum++) {
        	String[] line = lines.get(lineNum);
        	System.out.println("Line has " + line.length + " variables ");
        	String collectionName = line[0];

    		// Validate supplied collection name
    		List<String> collectionNameErrors = validateCollection(unitName, collectionName, lineNum);
        	response.addErrors(collectionNameErrors);

        	// If we have an invalid collection, no point on continuing with validation
    		if(collectionNameErrors.size()>0) {
    			return response;
    		}
        	
        	String sourceId = line[1];
        	String itemTitle = line[2];
        	String itemDescription = line[3];
        	
        	List<String> itemErrors = validateItemColumns(sourceId, itemTitle, itemDescription, lineNum);
        	
        	response.addErrors(itemErrors);
        	
        	System.out.println(String.format("Collection: %s Source: %s Item Title: %s Item Desc: %s", collectionName, sourceId, itemTitle, itemDescription));
        	
        	String primaryFile = line[4];
        	String primaryFileLabel = line[5];
        	String primaryFileDescr = line[6];
        	
        	SupplementType supplementType = getSupplementalFileType(line[7]);
        	
        	List<String> primaryFileErrors = validatePrimaryFile(unitName, collectionName, primaryFile, primaryFileLabel, supplementType, lineNum);
        	
        	response.addErrors(primaryFileErrors);
        	
        	System.out.println(String.format("PF Name: %s PF Label: %s PF Descr: %s", primaryFile, primaryFileLabel, primaryFileDescr));
        	
        	if(line.length>8) {
        		for(int c = 8; c < line.length; c++) {
        			String supplementalFile = line[c];
        			String supplementalFileLabel = "";
        			String supplementalFileDescr = "";
        			c++;
        			if(c < line.length) {
        				supplementalFileLabel = line[c];
        			}
        			c++;
        			if(c < line.length) {
        				supplementalFileDescr = line[c];
        			}
        			
        			List<String> supplementErrors = validateSupplement(unitName, collectionName,  supplementalFile, supplementalFileLabel, supplementType, lineNum);

                	response.addErrors(supplementErrors);
                	
                	System.out.println(
                			String.format("supplementalFile: %s supplementalFileLabel: %s supplementalFileDescr: %s", supplementalFile, supplementalFileLabel, supplementalFileDescr));
                	
        		}
        	}
        }
        
        return response;
	}
	
	private List<String[]> parse(String csvString) {

		List<String[]> lines = new ArrayList<String[]>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new StringReader(csvString));
            lines = reader.readAll();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
	}
	
	// Validation methods
	private List<String> validateItemColumns(String sourceId, String itemTitle, String itemDescription, int lineNum) {
		List<String> errors = new ArrayList<String>();
		
    	if(sourceId.isBlank()) {
    		errors.add(String.format("Row: %s: Source ID is missing", lineNum));
    	}
    	if(itemTitle.isBlank()) {
    		errors.add(String.format("Row: %s: Item Title is missing", lineNum));
    	}
    	if(itemDescription.isBlank()) {
    		errors.add(String.format("Row: %s: Item Description is missing", lineNum));
    	}
    	return errors;
	}
	private List<String> validatePrimaryFile(String unitName, String collectionName, String primaryFile, String primaryFileLabel, SupplementType supplementType, int lineNum){
		List<String> errors = new ArrayList<String>();
		
		if(supplementType == SupplementType.PRIMARYFILE) {
	    	if(primaryFile.isBlank()) {
	    		errors.add(String.format("Row: %s: Primary file name is missing for supplement type Primary", lineNum));
	    	}
	    	if(primaryFileLabel.isBlank()) {
	    		errors.add(String.format("Row: %s: Primary file label is missing for supplement type Primary", lineNum));
	    	}
		}
		else if(supplementType == SupplementType.ITEM || supplementType == SupplementType.COLLECTION) {
			if(!primaryFile.isBlank()) {
	    		errors.add(String.format("Row: %s: Primary file name should be blank for supplement type Item", lineNum));
			}
			if(!primaryFile.isBlank()) {
	    		errors.add(String.format("Row: %s: Primary file label should be blank for supplement type Item", lineNum));
			}
		}
		else {
			// Check to see if file exists in database
			List<Primaryfile> files = primaryFileRepository.findByLabel(primaryFileLabel);

			// If not - new file - Make sure it exists on file system
			if(files.size()==0) {
				if(!fileExists(unitName, collectionName, primaryFile)) {
		    		errors.add(String.format("Row: %s: Primary file %s does not exist in the dropbox", lineNum, primaryFile));
				}
			}
			
		}
    	return errors;
	}
	private List<String> validateSupplement(String unitName, String collectionName, String supplementalFile, String supplementalFileLabel, SupplementType supplementType, int lineNum){
		List<String> errors = new ArrayList<String>();
		if(supplementalFile.isBlank() && supplementalFileLabel.isBlank()) {
			return errors;
		}
		else if(supplementType==null) {
			if(!supplementalFile.isBlank()) {
	    		errors.add(String.format("Row: %s: Supplemental file name supplied without a supplement type", lineNum));
			}
			if(!supplementalFileLabel.isBlank()){
	    		errors.add(String.format("Row: %s: Supplemental file label supplied without a supplement type", lineNum));
			}
		}
		else if(supplementalFile.isBlank()) {
    		errors.add(String.format("Row: %s: Supplemental file name not supplied for supplement type %s", lineNum, supplementType));
		}
		else if(supplementalFileLabel.isBlank()) {
    		errors.add(String.format("Row: %s: Supplemental file label not supplied for supplement type %s", lineNum, supplementType));
		}
		else {
			if(supplementType==SupplementType.PRIMARYFILE) {
				// Check to see if file exists in database
				List<PrimaryfileSupplement> files = primaryFileSupplementRepository.findByLabel(supplementalFileLabel);

				// If not - new file - Make sure it exists on file system
				if(files.size()==0) {
					if(!fileExists(unitName, collectionName, supplementalFile)) {
			    		errors.add(String.format("Row: %s: Primary supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
					}
				}
			}
			else if(supplementType==SupplementType.ITEM){
				// Check to see if file exists in database
				List<ItemSupplement> files = itemSupplementRepository.findByLabel(supplementalFileLabel);

				// If not - new file - Make sure it exists on file system
				if(files.size()==0) {
					if(!fileExists(unitName, collectionName, supplementalFile)) {
			    		errors.add(String.format("Row: %s: Item supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
					}
				}
			}
			else if(supplementType==SupplementType.ITEM){
				// Check to see if file exists in database
				List<CollectionSupplement> files = collectionSupplementRepository.findByLabel(supplementalFileLabel);

				// If not - new file - Make sure it exists on file system
				if(files.size()==0) {
					if(!fileExists(unitName, collectionName, supplementalFile)) {
			    		errors.add(String.format("Row: %s: Collection supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
					}
				}
			}

		}
		
		return errors;
	}
	private List<String> validateUnit(String unitName){
		List<String> errors = new ArrayList<String>();
		if(unitName==null || unitName.isBlank()) {
    		errors.add("Missing unit name");
		}
		else if(!unitExists(unitName)) {
			errors.add(String.format("Invalid unit name supplied %s", unitName));
		}
		return errors;
	}
	private List<String> validateCollection(String unitName, String collectionName, int lineNum){
		List<String> errors = new ArrayList<String>();
		if(collectionName==null || collectionName.isBlank()) {
    		errors.add(String.format("Row %s: Missing collection name", lineNum));
		}
		else if(!collectionExists(collectionName)) {
			errors.add(String.format("Row %s: Invalid collection name supplied %s", lineNum, collectionName));
		}
		else if(!dropBoxExists(unitName, collectionName)) {
			errors.add(String.format("Row %s: Invalid drop box %s", lineNum, collectionName));
		}
		return errors;
	}
	
	// Helper methods
	private boolean dropBoxExists(String unit, String collection) {
		Path path = getCollectionPath(unit, collection);
		return Files.exists(path);
	}
	private boolean fileExists(String unit, String collection, String filename) {

		Path path = Paths.get(dropBoxBase, unit, collection, filename);	
		return Files.exists(path);
	}
	private Path getCollectionPath(String unit, String collection) {
		Path path = Paths.get(dropBoxBase, unit, collection);	
		return path;
	}
	private boolean unitExists(String unitName) {
		List<Unit> units = unitRepository.findByName(unitName);
		return units!=null && units.size()>0;
	}
	private boolean collectionExists(String collectionName) {
		List<Collection> collections = collectionRepository.findByName(collectionName);
		return collections!=null && collections.size()>0;
	}
	private SupplementType getSupplementalFileType(String supplementalFileType) {
		switch(supplementalFileType.trim().toLowerCase()) {
			case "p":
			case "primary":
				return SupplementType.PRIMARYFILE;
			case "i":
			case "item":
				return SupplementType.ITEM;
			case "c":
			case "collection":
				return SupplementType.COLLECTION;
			default:
				return null;
		}
	}
	
}
