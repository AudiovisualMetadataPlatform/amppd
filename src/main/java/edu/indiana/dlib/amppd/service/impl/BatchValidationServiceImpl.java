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
import edu.indiana.dlib.amppd.model.Batch;
import edu.indiana.dlib.amppd.model.BatchFile;
import edu.indiana.dlib.amppd.model.BatchFile.SupplementType;
import edu.indiana.dlib.amppd.model.BatchSupplementFile;
import edu.indiana.dlib.amppd.service.BatchValidationService;
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
public class BatchValidationServiceImpl implements BatchValidationService {
	
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
	
	
	private Batch createBatch(String unitName, List<String[]> lines) {
		Batch batch = new Batch();
		
		List<Unit> units = unitRepository.findByName(unitName);
		
		if(units!=null && units.size()>0) {
			batch.setUnit(units.get(0));
		}
				
        for(int rowNum = 1; rowNum < lines.size(); rowNum++) {
        	
        	BatchFile batchFile = new BatchFile();	
        	batchFile.setRowNum(rowNum);
        	batch.addBatchFile(batchFile);
        	
        	String[] line = lines.get(rowNum);
        	
        	if(line.length < 7) continue;
        	List<Collection> collections = collectionRepository.findByName(line[0]);
        	
        	if(collections!=null && collections.size()>0) {
        		batchFile.setCollection(collections.get(0));
        	}
        	        	
        	batchFile.setSourceIdType(line[1]);
        	batchFile.setSourceId(line[2]);
        	batchFile.setItemName(line[3]);
        	batchFile.setItemDescription(line[4]);
        	        	
        	batchFile.setPrimaryfileFilename(line[5]);
        	batchFile.setPrimaryfileName(line[6]);
        	
        	// Description is optional.  Verify the array is long enough before continuing
        	if(line.length>=8) {
        		batchFile.setPrimaryfileDescription(line[7]);
        	}
        	else {
        		continue;
        	}
        	
        	SupplementType supplementType = null;
        	
        	// If a supplement type is supplied, get the enum value for the textual value
        	if(line.length>8) {
        		supplementType = getSupplementalFileType(line[8]);
        	}
        	
        	batchFile.setSupplementType(supplementType);
        	
        	if(line.length>9) {
        		int supplementNum = 1;
        		for(int c = 9; c < line.length; c++) {
        			BatchSupplementFile supplement = new BatchSupplementFile();
        			supplement.setSupplementNum(supplementNum);
        			supplement.setSupplementFilename(line[c]);
        			c++;
        			if(c < line.length) {
        				supplement.setSupplementName(line[c]);
        			}
        			c++;
        			if(c < line.length) {
        				supplement.setSupplementDescription(line[c]);
        			}
        			               	
        			batchFile.addSupplement(supplement);
                	supplementNum++;
        		}
        	}
        }
        
        return batch;
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

		Batch batch = createBatch(unitName, lines);
		
		// Validate supplied unit name
		List<String> unitErrors = validateUnit(batch.getUnit());
    	response.addErrors(unitErrors);
		
    	// If we have an invalid unit, no point on continuing with validation
		if(unitErrors.size()>0) {
			return response;
		}

				
        for(BatchFile batchFile : batch.getBatchFiles()) {
        	
    		// Validate supplied collection name
    		List<String> collectionNameErrors = validateCollection(batch.getUnit(), batchFile.getCollection(), batchFile.getRowNum());
        	response.addErrors(collectionNameErrors);

        	// If we have an invalid collection, no point on continuing with validation
    		if(collectionNameErrors.size()>0) {
    			return response;
    		}
    		
        	List<String> itemErrors = validateItemColumns(batchFile.getSourceId(), batchFile.getItemName(), batchFile.getRowNum());
        	response.addErrors(itemErrors);
        	
        	List<String> primaryFileErrors = validatePrimaryFile(batch.getUnit(), batchFile.getCollection(), batchFile.getPrimaryfileFilename(), batchFile.getPrimaryfileName(), batchFile.getSupplementType(), batchFile.getRowNum());
        	response.addErrors(primaryFileErrors);
        	

        	List<String> duplicatePrimaryFileErrors = validateUniquePrimaryfile(batch, batchFile);
        	response.addErrors(duplicatePrimaryFileErrors);
        	
        	
    		for(BatchSupplementFile supplement : batchFile.getBatchSupplementFiles()) {
    			List<String> supplementErrors = validateSupplement(batch.getUnit(), batchFile.getCollection(),  supplement.getSupplementFilename(), supplement.getSupplementName(), batchFile.getSupplementType(), batchFile.getRowNum());
    			response.addErrors(supplementErrors);
    			
    			List<String> duplicateSupplementErrors = validateUniqueSupplement(batch, batchFile, supplement);
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
	
	private List<String> validateUniquePrimaryfile(Batch batch, BatchFile batchFile) {
		List<String> errors = new ArrayList<String>();
		
		if(batch.isDuplicatePrimaryfileFilename(batchFile.getPrimaryfileFilename(), batchFile.getRowNum())) {
    		errors.add(String.format("Row: %s: Duplicate primary file %s", batchFile.getRowNum(), batchFile.getPrimaryfileFilename()));
		}
		
		if(batch.isDuplicatePrimaryfileName(batchFile.getPrimaryfileName(), batchFile.getRowNum())) {
    		errors.add(String.format("Row: %s: Duplicate primary file name %s", batchFile.getRowNum(), batchFile.getPrimaryfileName()));
		}
    	return errors;
	}

	private List<String> validateUniqueSupplement(Batch batch, BatchFile batchFile, BatchSupplementFile batchSupplementFile) {
		List<String> errors = new ArrayList<String>();
		for(BatchFile testRow : batch.getBatchFiles()) {
			if(testRow.getSupplementType() == null) continue;
			if(testRow.containsSupplementFilename(batchSupplementFile.getSupplementFilename(), batchFile.getRowNum(), batchSupplementFile.getSupplementNum())){
	    		errors.add(String.format("Row: %s: Duplicate supplement file %s", batchFile.getRowNum(), batchSupplementFile.getSupplementFilename()));
			}
			if(testRow.containsSupplementName(batchSupplementFile.getSupplementName(), batchFile.getRowNum(), batchSupplementFile.getSupplementNum())){
	    		errors.add(String.format("Row: %s: Duplicate supplement name %s", batchFile.getRowNum(), batchSupplementFile.getSupplementName()));
			}
			if(errors.size()>0) break;
		}
		
    	return errors;
	}
	private List<String> validatePrimaryFile(Unit unit, Collection collection, String primaryFile, String primaryFileLabel, SupplementType supplementType, int lineNum){
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
				if(!primaryFile.isBlank() && !fileExists(unit.getName(), collection.getName(), primaryFile)) {
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
	private List<String> validateSupplement(Unit unit, Collection collection, String supplementalFile, String supplementalFileLabel, SupplementType supplementType, int lineNum){
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
					if(!fileExists(unit.getName(), collection.getName(), supplementalFile)) {
			    		errors.add(String.format("Row: %s: Primary supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
					}
				}
			}
			else if(supplementType==SupplementType.ITEM){
				// Check to see if file exists in database
				List<ItemSupplement> files = itemSupplementRepository.findByLabel(supplementalFileLabel);

				// If not - new file - Make sure it exists on file system
				if(files.size()==0) {
					if(!fileExists(unit.getName(), collection.getName(), supplementalFile)) {
			    		errors.add(String.format("Row: %s: Item supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
					}
				}
			}
			else if(supplementType==SupplementType.ITEM){
				// Check to see if file exists in database
				List<CollectionSupplement> files = collectionSupplementRepository.findByLabel(supplementalFileLabel);

				// If not - new file - Make sure it exists on file system
				if(files.size()==0) {
					if(!fileExists(unit.getName(), collection.getName(), supplementalFile)) {
			    		errors.add(String.format("Row: %s: Collection supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
					}
				}
			}

		}
		
		return errors;
	}
	private List<String> validateUnit(Unit unit){
		String unitName = unit!=null ? unit.getName() : "";
		List<String> errors = new ArrayList<String>();
		if(unitName==null || unitName.isBlank()) {
    		errors.add("Missing unit name");
		}
		else if(!unitExists(unitName)) {
			errors.add(String.format("Invalid unit name supplied %s", unitName));
		}
		return errors;
	}
	private List<String> validateCollection(Unit unit, Collection collection, int lineNum){
		List<String> errors = new ArrayList<String>();
		String collectionName = collection!=null ? collection.getName() : "";
		if(collectionName==null || collectionName.isBlank()) {
    		errors.add(String.format("Row %s: Missing collection name", lineNum));
		}
		else if(!collectionExists(collectionName)) {
			errors.add(String.format("Row %s: Invalid collection name supplied %s", lineNum, collection.getName()));
		}
		else if(!dropBoxExists(unit.getName(), collectionName)) {
			errors.add(String.format("Row %s: Invalid drop box %s", lineNum, collection.getName()));
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
