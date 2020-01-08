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

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.BatchFile.SupplementType;
import edu.indiana.dlib.amppd.service.ManifestValidationService;
import edu.indiana.dlib.amppd.service.model.Manifest;
import edu.indiana.dlib.amppd.service.model.ManifestRow;
import edu.indiana.dlib.amppd.service.model.ManifestSupplement;
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
	
	@Autowired
	private AmppdPropertyConfig propertyConfig;
	
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
	
	
	private Manifest createManifest(String unitName, List<String[]> lines) {
		Manifest manifest = new Manifest();
		
		manifest.setUnitName(unitName);
				
        for(int rowNum = 1; rowNum < lines.size(); rowNum++) {
        	
        	ManifestRow manifestRow = new ManifestRow();	
        	manifestRow.setRowNum(rowNum);
        	manifest.addRow(manifestRow);
        	
        	String[] line = lines.get(rowNum);
        	
        	if(line.length < 7) continue;
        	
        	manifestRow.setCollectionName(line[0]);
        	
    		manifestRow.setSourceIdType(line[1]);
    		manifestRow.setSourceId(line[2]);
    		manifestRow.setItemName(line[3]);
    		manifestRow.setItemDescription(line[4]);
        	        	
        	manifestRow.setPrimaryfileFilename(line[5]);
        	manifestRow.setPrimaryfileName(line[6]);
        	
        	// Description is optional.  Verify the array is long enough before continuing
        	if(line.length>=8) {
            	manifestRow.setPrimaryfileDescription(line[7]);
        	}
        	else {
        		continue;
        	}
        	
        	SupplementType supplementType = null;
        	
        	// If a supplement type is supplied, get the enum value for the textual value
        	if(line.length>8) {
        		supplementType = getSupplementalFileType(line[8]);
        	}
        	
        	manifestRow.setSupplementType(supplementType);
        	
        	if(line.length>9) {
        		int supplementNum = 1;
        		for(int c = 9; c < line.length; c++) {
        			ManifestSupplement supplement = new ManifestSupplement();
        			supplement.setSupplementNum(supplementNum);
        			supplement.setFilename(line[c]);
        			c++;
        			if(c < line.length) {
        				supplement.setName(line[c]);
        			}
        			c++;
        			if(c < line.length) {
        				supplement.setDescription(line[c]);
        			}
        			               	
                	manifestRow.addSupplement(supplement);
                	supplementNum++;
        		}
        	}
        }
        
        return manifest;
	}
	
	public ValidationResponse validate(String unitName, String fileContent) {

		ValidationResponse response = new ValidationResponse();
		
		// Turn the string into a list of string arrays representing rows
		List<String[]> lines = parse(fileContent);
		
		// If we have no rows, quit now
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

		Manifest manifest = createManifest(unitName, lines);
				
        for(ManifestRow manifestRow : manifest.getRows()) {
        	
    		// Validate supplied collection name
    		List<String> collectionNameErrors = validateCollection(manifest.getUnitName(), manifestRow.getCollectionName(), manifestRow.getRowNum());
        	response.addErrors(collectionNameErrors);

        	// If we have an invalid collection, no point on continuing with validation
    		if(collectionNameErrors.size()>0) {
    			return response;
    		}
    		
        	List<String> itemErrors = validateItemColumns(manifestRow.getSourceId(), manifestRow.getItemName(), manifestRow.getRowNum());
        	response.addErrors(itemErrors);
        	
        	List<String> primaryFileErrors = validatePrimaryFile(manifest.getUnitName(), manifestRow.getCollectionName(), manifestRow.getPrimaryfileFilename(), manifestRow.getPrimaryfileName(), manifestRow.getSupplementType(), manifestRow.getRowNum());
        	response.addErrors(primaryFileErrors);
        	

        	List<String> duplicatePrimaryFileErrors = validateUniquePrimaryfile(manifest, manifestRow);
        	response.addErrors(duplicatePrimaryFileErrors);
        	
        	
    		for(ManifestSupplement supplement : manifestRow.getSupplements()) {
    			List<String> supplementErrors = validateSupplement(manifest.getUnitName(), manifestRow.getCollectionName(),  supplement.getFilename(), supplement.getName(), manifestRow.getSupplementType(), manifestRow.getRowNum());
    			response.addErrors(supplementErrors);
    			
    			List<String> duplicateSupplementErrors = validateUniqueSupplement(manifest, manifestRow, supplement);
    			response.addErrors(duplicateSupplementErrors);
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
	private List<String> validateItemColumns(String sourceId, String itemTitle, int lineNum) {
		List<String> errors = new ArrayList<String>();
		
    	if(sourceId.isBlank()) {
    		errors.add(String.format("Row: %s: Source ID is missing", lineNum));
    	}
    	if(itemTitle.isBlank()) {
    		errors.add(String.format("Row: %s: Item Title is missing", lineNum));
    	}
    	return errors;
	}
	
	private List<String> validateUniquePrimaryfile(Manifest manifest, ManifestRow manifestRow) {
		List<String> errors = new ArrayList<String>();
		
		if(manifest.isDuplicatePrimaryfileFilename(manifestRow.getPrimaryfileFilename(), manifestRow.getRowNum())) {
    		errors.add(String.format("Row: %s: Duplicate primary file %s", manifestRow.getRowNum(), manifestRow.getPrimaryfileFilename()));
		}
		
		if(manifest.isDuplicatePrimaryfileName(manifestRow.getPrimaryfileName(), manifestRow.getRowNum())) {
    		errors.add(String.format("Row: %s: Duplicate primary file name %s", manifestRow.getRowNum(), manifestRow.getPrimaryfileName()));
		}
    	return errors;
	}

	private List<String> validateUniqueSupplement(Manifest manifest, ManifestRow manifestRow, ManifestSupplement manifestSupplement) {
		List<String> errors = new ArrayList<String>();
		for(ManifestRow testRow : manifest.getRows()) {
			if(testRow.getSupplementType() == null) continue;
			if(testRow.containsSupplementFilename(manifestSupplement.getFilename(), manifestRow.getRowNum(), manifestSupplement.getSupplementNum())){
	    		errors.add(String.format("Row: %s: Duplicate supplement file %s", manifestRow.getRowNum(), manifestSupplement.getFilename()));
			}
			if(testRow.containsSupplementName(manifestSupplement.getName(), manifestRow.getRowNum(), manifestSupplement.getSupplementNum())){
	    		errors.add(String.format("Row: %s: Duplicate supplement name %s", manifestRow.getRowNum(), manifestSupplement.getName()));
			}
			if(errors.size()>0) break;
		}
		
    	return errors;
	}
	private List<String> validatePrimaryFile(String unitName, String collectionName, String primaryFile, String primaryFileLabel, SupplementType supplementType, int lineNum){
		List<String> errors = new ArrayList<String>();
		
		// If the supplement type is for a primary file, or no supplement is supplied, we need make sure primary file values are supplied
		if(supplementType == SupplementType.PRIMARYFILE || supplementType == null) {
	    	if(primaryFile.isBlank()) {
	    		if(supplementType == SupplementType.PRIMARYFILE) {
		    		errors.add(String.format("Row: %s: Primary file name is missing for supplement type Primary", lineNum));
	    		}
	    		else {
		    		errors.add(String.format("Row: %s: Primary file name is missing", lineNum));
	    		}
	    	}
	    	if(primaryFileLabel.isBlank()) {
	    		if(supplementType == SupplementType.PRIMARYFILE) {
		    		errors.add(String.format("Row: %s: Primary file label is missing for supplement type Primary", lineNum));
	    		}
	    		else {
		    		errors.add(String.format("Row: %s: Primary file label is missing", lineNum));
	    		}
	    	}

			// Check to see if file exists in database
			List<Primaryfile> files = primaryFileRepository.findByLabel(primaryFileLabel);

			// If not - new file - Make sure it exists on file system
			if(files.size()==0) {
				if(!primaryFile.isBlank() && !fileExists(unitName, collectionName, primaryFile)) {
		    		errors.add(String.format("Row: %s: Primary file %s does not exist in the dropbox", lineNum, primaryFile));
				}
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

		Path path = Paths.get(propertyConfig.getDropboxRoot(), unit, collection, filename);	
		return Files.exists(path);
	}
	private Path getCollectionPath(String unit, String collection) {
		Path path = Paths.get(propertyConfig.getDropboxRoot(), unit, collection);	
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
