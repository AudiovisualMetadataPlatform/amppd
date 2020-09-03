package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Batch;
import edu.indiana.dlib.amppd.model.BatchFile;
import edu.indiana.dlib.amppd.model.BatchFile.SupplementType;
import edu.indiana.dlib.amppd.model.BatchSupplementFile;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.BatchFileRepository;
import edu.indiana.dlib.amppd.repository.BatchRepository;
import edu.indiana.dlib.amppd.repository.BatchSupplementFileRepository;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.BatchValidationService;
import edu.indiana.dlib.amppd.service.DropboxService;
import edu.indiana.dlib.amppd.web.BatchValidationResponse;

@Service
@Transactional
public class BatchValidationServiceImpl implements BatchValidationService {
	
	@Autowired
    private UnitRepository unitRepository;
	
	@Autowired
    private CollectionRepository collectionRepository;
		
	@Autowired
    private BatchRepository batchRepository;
	
	@Autowired
    private BatchFileRepository batchFileRepository;
	
	@Autowired
    private BatchSupplementFileRepository batchSupplementFileRepository;

	@Autowired
	private DropboxService dropboxService;

	
	public BatchValidationResponse validateBatch(String unitName, AmpUser user, MultipartFile file) {
		BatchValidationResponse response;
		StringBuilder textBuilder = new StringBuilder();
		try (InputStream inputStream = file.getInputStream()) {
		    try (Reader reader = new BufferedReader(new InputStreamReader
		      (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
		        int c = 0;
		        while ((c = reader.read()) != -1) {
		            textBuilder.append((char) c);
		        }
		    }
		   
		} catch (IOException e) {
			response = new BatchValidationResponse();
			response.addError("Unable to parse CSV file");
			return response;
		}
		return validate(unitName, file.getOriginalFilename(), user, textBuilder.toString());
	}
	
	/*
	 * Create a batch object based on the parsed lines of the CSV
	 */
	private Batch createBatch(String unitName, String filename, AmpUser user, List<String[]> lines) {
		Batch batch = new Batch();
		batch.setManifestFilename(filename);
		batch.setSubmitUser(user);
		batch.setSubmitTime(new Date());
		// Get and set the unit
		List<Unit> units = unitRepository.findByName(unitName);
		
		if(units!=null && units.size()>0) {
			batch.setUnit(units.get(0));
		}
		
		// For each row in the CSV
        for(int rowNum = 1; rowNum < lines.size(); rowNum++) {
        	
        	// Create a new Batch File
        	BatchFile batchFile = new BatchFile();	
        	batchFile.setBatch(batch);
        	batchFile.setRowNum(rowNum);
        	batch.addBatchFile(batchFile);
        	
        	String[] line = lines.get(rowNum);
        	
        	// If we don't have enough values in this line, try the next line
        	if(line.length < 7) continue;
        	
        	// Get the collection
        	List<Collection> collections = collectionRepository.findByName(line[0]);
        	if(collections!=null && collections.size()>0) {
        		batchFile.setCollection(collections.get(0));
        	}
        	//Get the collection name
        	if(line[0] != null)
        		batchFile.setCollectionName(line[0]);
        	// Get the source and item        	
        	batchFile.setExternalSource(line[1]);
        	batchFile.setExternalItemId(line[2]);
        	batchFile.setItemName(line[3]);
        	batchFile.setItemDescription(line[4]);
        	        	
        	// Get the primary file info
        	batchFile.setPrimaryfileFilename(line[5]);
        	batchFile.setPrimaryfileName(line[6]);
        	
        	// Description is optional.  Verify the array is long enough before continuing
        	if(line.length>=8) {
        		batchFile.setPrimaryfileDescription(line[7]);
        	}
        	else {
        		continue;
        	}
        	
        	// Process supplements
        	SupplementType supplementType = null;
        	
        	// If a supplement type is supplied, get the enum value for the textual value
        	if(line.length>8) {
        		supplementType = getSupplementalFileType(line[8]);
        	}
        	
        	batchFile.setSupplementType(supplementType);
        	
        	// Iterate through the supplements.  There can be variable number of supplements
        	if(line.length>9) {
        		int supplementNum = 1;
        		for(int c = 9; c < line.length; c++) {
        			BatchSupplementFile supplement = new BatchSupplementFile();
        			supplement.setBatchFile(batchFile);
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
        			// If the values are blank, don't add them
        			if(supplement.getSupplementName()!=null && supplement.getSupplementName().isBlank() && supplement.getSupplementFilename().isBlank()) continue;        	
        			batchFile.addSupplement(supplement);
                	supplementNum++;
        		}
        	}
        }
        return batch;
	}
	
	/*
	 * Validate the CSV
	 */
	public BatchValidationResponse validate(String unitName, String filename, AmpUser user, String fileContent) {

		BatchValidationResponse response = new BatchValidationResponse();
		
		// Turn the string into a list of string arrays representing rows
		List<String[]> lines = parse(fileContent);
		
		// If we have no rows, quit now
		if(lines.size()<=1) {
			response.addError("Invalid file. No rows supplied.");
			return response;
		}

		Batch batch = createBatch(unitName, filename, user, lines);
		
		// Validate supplied unit name
		List<String> unitErrors = validateUnit(batch.getUnit());
    	response.addErrors(unitErrors);
		
    	// If we have an invalid unit, no point on continuing with validation
		if(unitErrors.size()>0) {
			return response;
		}

				
        for(BatchFile batchFile : batch.getBatchFiles()) {
        	
    		// Validate supplied collection name
    		List<String> collectionNameErrors = validateCollection(batch.getUnit(), batchFile.getCollection(), batchFile.getRowNum(), batchFile.getCollectionName());
        	response.addErrors(collectionNameErrors);

        	// If we have an invalid collection, no point on continuing with validation
    		if(collectionNameErrors.size()>0) {
    			//return response;
    			continue;
    		}
    		
        	List<String> itemErrors = validateItemColumns( batchFile.getItemName(), batchFile.getRowNum());
        	response.addErrors(itemErrors);
        	
        	// Validate the primary file
        	List<String> primaryFileErrors = validatePrimaryFile(batch.getUnit(), batchFile.getCollection(), batchFile.getPrimaryfileFilename(), batchFile.getPrimaryfileName(), batchFile.getSupplementType(), batchFile.getRowNum());
        	response.addErrors(primaryFileErrors);
        	
        	// Check for duplicate primary files
        	List<String> duplicatePrimaryFileErrors = validateUniquePrimaryfile(batch, batchFile);
        	response.addErrors(duplicatePrimaryFileErrors);
        	
        	// For each supplement, validate the values and make sure there are no duplicates
    		for(BatchSupplementFile supplement : batchFile.getBatchSupplementFiles()) {
    			List<String> supplementErrors = validateSupplement(batch.getUnit(), batchFile.getCollection(),  supplement.getSupplementFilename(), supplement.getSupplementName(), batchFile.getSupplementType(), batchFile.getRowNum());
    			response.addErrors(supplementErrors);
    			
    			List<String> duplicateSupplementErrors = validateUniqueSupplement(batch, batchFile, supplement);
    			response.addErrors(duplicateSupplementErrors);
    		}
        }
        
        // If we have no errors, save the batch and add it to the response
        if(!response.hasErrors()) {
        	batchRepository.save(batch);
        	batchFileRepository.saveAll(batch.getBatchFiles());
        	
        	for(BatchFile batchFile : batch.getBatchFiles()) {
        		batchSupplementFileRepository.saveAll(batchFile.getBatchSupplementFiles());
        	}
        	
        	response.setBatch(batch);
        	
        	response.setSuccess(true);
        }
        
        return response;
	}
	
	/*
	 * Turn the string into a list of arrays representing lines
	 */
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
	
	/*
	 * Validate item columns
	 */
	private List<String> validateItemColumns( String itemTitle, int lineNum) {
		List<String> errors = new ArrayList<String>();
		
    	if(itemTitle.isBlank()) {
    		errors.add(String.format("Row: %s: Item Title is missing", lineNum));
    	}
    	
    	return errors;
	}
	
	/*
	 * Make sure primary files are unique to this file
	 */
	private List<String> validateUniquePrimaryfile(Batch batch, BatchFile batchFile) {
		List<String> errors = new ArrayList<String>();
		
		if(batch.isDuplicatePrimaryfileFilename(batchFile.getPrimaryfileFilename(), batchFile.getRowNum())) {
    		errors.add(String.format("Row: %s: Duplicate primary file %s", batchFile.getRowNum(), batchFile.getPrimaryfileFilename()));
		}
		
		if(batch.isDuplicatePrimaryfileName(batchFile.getPrimaryfileName(), batchFile.getExternalItemId(), batchFile.getItemName(), batchFile.getRowNum())) {
    		errors.add(String.format("Row: %s: Duplicate primary file name %s", batchFile.getRowNum(), batchFile.getPrimaryfileName()));
		}
    	return errors;
	}
	
	/*
	 * Make sure supplements are unique to this file
	 */
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
	
	/*
	 * Validate the primary file values
	 */
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
			boolean primaryFileExists = primaryFileExistsInCollection(collection, primaryFileLabel);

			// If not - new file - Make sure it exists on file system
			if(!primaryFileExists) {
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
	
	/*
	 * Check to see if this primary file exists in a collection already
	 */
	private boolean primaryFileExistsInCollection(Collection collection, String name) {
		if(collection.getItems() == null) {
			return false;
		}
		
		for(Item item : collection.getItems()) {
			if(item.getPrimaryfiles()==null) {
				continue;
			}
			for(Primaryfile primaryFile : item.getPrimaryfiles()) {
				if(primaryFile.getName()==name) {
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Validate the supplement
	 */
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
				if(!fileExists(unit.getName(), collection.getName(), supplementalFile)) {
		    		errors.add(String.format("Row: %s: Primary supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
				}
				
			}
			else if(supplementType==SupplementType.ITEM){
				if(!fileExists(unit.getName(), collection.getName(), supplementalFile)) {
		    		errors.add(String.format("Row: %s: Item supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
				}
			}
			else if(supplementType==SupplementType.ITEM){
				if(!fileExists(unit.getName(), collection.getName(), supplementalFile)) {
			    	errors.add(String.format("Row: %s: Collection supplement file %s does not exist in the dropbox", lineNum, supplementalFile));
				}
			}

		}
		
		return errors;
	}
	/*
	 * Validate the unit
	 */
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
	/*
	 * Validate the collection
	 */
	private List<String> validateCollection(Unit unit, Collection collection, int lineNum, String collectionNameFromManifest){
		List<String> errors = new ArrayList<String>();
		String collectionName = collection!=null ? collection.getName() : "";
		if(collectionNameFromManifest == null || collectionNameFromManifest.isBlank()) {
			errors.add(String.format("Row %s: Missing collection name", lineNum));
		}
		else if((collectionName==null || collectionName.isBlank()) ) {
			errors.add(String.format("Row %s: Collection does not exist", lineNum));
		}
		else if(!collectionExists(collectionName)) {
			errors.add(String.format("Row %s: Invalid collection name supplied %s", lineNum, collectionNameFromManifest));
		}
		else if(!dropBoxExists(unit.getName(), collectionName)) {
			errors.add(String.format("Row %s: Invalid drop box %s", lineNum, collection.getName()));
		}
		return errors;
	}
	
	/*
	 * Verify the drop box exists fot this collection
	 */
	private boolean dropBoxExists(String unit, String collection) {
		Path path = getCollectionPath(unit, collection);
		return Files.exists(path);
	}
	/*
	 * Verify the file exists in the drop box
	 */
	private boolean fileExists(String unit, String collection, String filename) {
		Path collectionPath = getCollectionPath(unit, collection);
		Path path = Paths.get(collectionPath.toString(), filename);
		return Files.exists(path);
	}
	/*
	 * Get the collection path in the drop box
	 */
	private Path getCollectionPath(String unit, String collection) {
		return dropboxService.getDropboxPath(unit, collection);
	}
	/*
	 * Verify the unit exists in the database 
	 */
	private boolean unitExists(String unitName) {
		List<Unit> units = unitRepository.findByName(unitName);
		return units!=null && units.size()>0;
	}
	/*
	 * Verify the collection exists in the database
	 */
	private boolean collectionExists(String collectionName) {
		List<Collection> collections = collectionRepository.findByName(collectionName);
		return collections!=null && collections.size()>0;
	}
	/*
	 * Convert supplemental file type string to an enum
	 */
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