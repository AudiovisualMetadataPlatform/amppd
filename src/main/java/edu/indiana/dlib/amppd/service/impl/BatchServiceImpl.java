package edu.indiana.dlib.amppd.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.indiana.dlib.amppd.model.Batch;
import edu.indiana.dlib.amppd.model.BatchFile;
import edu.indiana.dlib.amppd.model.BatchFile.FileType;

public class BatchServiceImpl {
	public void validate(Batch batch) {
		for(BatchFile batchFile : batch.getBatchFiles()) {
			validateFile(batch, batchFile);
		}
	}
	
	private boolean validateFile(Batch batch, BatchFile batchFile) {
		boolean requiredFieldsProvided = requiredFieldsProvided(batchFile);
		boolean collectionNameExists = collectionNameExists(batchFile);
		boolean dropBoxExists = dropBoxExists(batch.getCollection().getUnit().getName(), batch.getCollection().getName(), batchFile);
		boolean fileExists = fileExists(batchFile);
		
		return requiredFieldsProvided && collectionNameExists && dropBoxExists && fileExists;
	}
	
	private boolean requiredFieldsProvided(BatchFile batchFile) {
		
		String sourceId = batchFile.getSourceId();
		String sourceIdLabel = batchFile.getSourceId();
		
		if(sourceId.isBlank()) {
			
		}
		if(sourceIdLabel.isBlank()) {
			
		}
		
		String itemName = batchFile.getItemName();
		String itemDescr = batchFile.getItemDescription();
		
		if(itemName.isBlank()) {
			
		}

		if(itemDescr.isBlank()) {
			
		}
		
		String primaryFile = batchFile.getPrimaryfileFilename();
		String primaryFileLabel = batchFile.getPrimaryfileName();
		String primaryFileDescription = batchFile.getPrimaryfileDescription();
		
		if(!primaryFile.isBlank()) {

			if(primaryFileLabel.isBlank()) {
				
			}

			if(primaryFileDescription.isBlank()) {
				
			}
		}
		
		String supplementalFile = batchFile.getSupplementFilename();
		// TODO:  Need accessors for these
		String supplementalFileLabel = "";
		String supplementalFileDescription = "";
		
		if(!supplementalFile.isBlank()) {
			
		}
		
		
		return false;
	}
	private boolean collectionNameExists(BatchFile batchFile) {
		return false;
	}
	private boolean dropBoxExists(String unit, String collection, BatchFile batchFile) {
		Path path = getCollectionPath(unit, collection);
		return Files.exists(path);
	}
	private boolean fileExists(BatchFile batchFile) {
		
		return false;
	}
	
	private Path getCollectionPath(String unit, String collection) {
		Path path = Paths.get("/srv/amp/", unit, collection);	
		
		return path;
	}
}
