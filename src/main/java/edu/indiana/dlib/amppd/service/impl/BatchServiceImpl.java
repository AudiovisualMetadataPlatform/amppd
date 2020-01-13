package edu.indiana.dlib.amppd.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.BatchFile.SupplementType;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.service.BatchService;
import edu.indiana.dlib.amppd.service.model.Manifest;
import edu.indiana.dlib.amppd.service.model.ManifestRow;
import edu.indiana.dlib.amppd.service.model.ManifestSupplement;
import edu.indiana.dlib.amppd.web.ValidationResponse;

public class BatchServiceImpl implements BatchService {
	@Autowired
	private AmppdPropertyConfig propertyConfig;
	
	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
	private CollectionRepository collectionRepository;
	
	public boolean processBatch(ValidationResponse batchValidation) {
		try {
			
			Manifest manifest = batchValidation.getManifest();
			
			for(ManifestRow row : manifest.getRows()) {
				
				// If we have a primary file, move it to the destination
				if(row.getSupplementType()==SupplementType.PRIMARYFILE || row.getSupplementType() == null) {
					moveFile(manifest.getUnitName(), row.getCollectionName(), row.getPrimaryfileFilename());
				}
				
				if(row.getSupplementType() != null) {
					for(ManifestSupplement supplement : row.getSupplements()) {
						moveFile(manifest.getUnitName(), row.getCollectionName(), supplement.getFilename());
					}
				}
				createItem(row,"TODO");
			}	
			
			return true;
		}
		catch(Exception ex) {
			cleanup();
			return false;
		}
	}
	private void cleanup() {
		
	}
	
	private void createItem(ManifestRow row, String username) {
		
		List<Collection> collections =  collectionRepository.findByName(row.getCollectionName());
		
		Collection collection = collections.get(0);

		Set<Item> items = collection.getItems();

		Item item = getItem(row.getItemName(), row.getItemDescription(), username);
		items.add(item);
		
		if(row.getSupplementType()==SupplementType.PRIMARYFILE || row.getSupplementType()==null) {
			// Create Primary files
			Primaryfile primaryFile = new Primaryfile();
			primaryFile.setName(row.getPrimaryfileName());
			primaryFile.setOriginalFilename(row.getPrimaryfileFilename());
			primaryFile.setCreatedBy(username);
			primaryFile.setModifiedBy(username);
			primaryFile.setCreatedDate(new Date().getTime());
			primaryFile.setModifiedDate(new Date().getTime());
			
			primaryFile.setItem(item);
			
			if(row.getSupplementType()==SupplementType.PRIMARYFILE) {

				Set<PrimaryfileSupplement> primaryfileSupplements = new HashSet<PrimaryfileSupplement>();
				// Create Supplements
				for(ManifestSupplement supplement : row.getSupplements()) {
					PrimaryfileSupplement primaryfileSupplement = new PrimaryfileSupplement();
					primaryfileSupplement.setPrimaryfile(primaryFile);
					primaryfileSupplement.setName(supplement.getName());
					primaryfileSupplement.setOriginalFilename(supplement.getFilename());
					primaryfileSupplement.setDescription(supplement.getDescription());
					primaryfileSupplement.setCreatedBy(username);
					primaryfileSupplement.setModifiedBy(username);
					primaryfileSupplement.setCreatedDate(new Date().getTime());
					primaryfileSupplement.setModifiedDate(new Date().getTime());
					primaryfileSupplements.add(primaryfileSupplement);
				}
				primaryFile.setSupplements(primaryfileSupplements);
			}
		}
		else if(row.getSupplementType()==SupplementType.COLLECTION) {

			Set<CollectionSupplement> collectionSupplements = collection.getSupplements();
			
			// Create Supplements
			for(ManifestSupplement supplement : row.getSupplements()) {
				CollectionSupplement collectionSupplement = new CollectionSupplement();
				collectionSupplement.setCollection(collection);
				collectionSupplement.setName(supplement.getName());
				collectionSupplement.setOriginalFilename(supplement.getFilename());
				collectionSupplement.setDescription(supplement.getDescription());
				collectionSupplement.setCreatedBy(username);
				collectionSupplement.setModifiedBy(username);
				collectionSupplement.setCreatedDate(new Date().getTime());
				collectionSupplement.setModifiedDate(new Date().getTime());
				collectionSupplements.add(collectionSupplement);
			}
		}
		else if(row.getSupplementType()==SupplementType.ITEM) {
			Set<ItemSupplement> itemSupplements = item.getSupplements();
			// Create Supplements
			for(ManifestSupplement supplement : row.getSupplements()) {
				ItemSupplement itemSupplement = new ItemSupplement();
				itemSupplement.setItem(item);
				itemSupplement.setName(supplement.getName());
				itemSupplement.setOriginalFilename(supplement.getFilename());
				itemSupplement.setDescription(supplement.getDescription());
				itemSupplement.setCreatedBy(username);
				itemSupplement.setModifiedBy(username);
				itemSupplement.setCreatedDate(new Date().getTime());
				itemSupplement.setModifiedDate(new Date().getTime());
				itemSupplements.add(itemSupplement);
			}
		}
		
	}
	
	private Item getItem(String itemName, String itemDescription, String createdBy) {
		Item item = getExistingItem(itemName);
		
		// Check if it already exists
		if(item==null) {
			// if not, create it
			item = new Item();
			item.setName(itemName);
			item.setDescription(itemDescription);
			item.setCreatedBy(createdBy);
			item.setModifiedBy(createdBy);
			item.setCreatedDate(new Date().getTime());
			item.setModifiedDate(new Date().getTime());
			itemRepository.save(item);
		}
		
		return item;
	}
	
	private Item getExistingItem(String itemName) {
		Item item = null;
		
		List<Item> items = itemRepository.findByName(itemName);
		
		if(items.size()>0) {
			item = items.get(0);
		}
		
		return item;
	}
	
	
	
		
	
	private void moveFile(String unitName, String collectionName, String fileName) throws IOException {
		
		// Create paths from /{root}/{unit}/{collection}/{filename}
		Path existingFile = Paths.get(propertyConfig.getDropboxRoot(), unitName, collectionName, fileName);	
		Path newLink = Paths.get(propertyConfig.getFileStorageRoot(), unitName, collectionName, fileName);	
		
		// Create a hard link
		Files.createLink(newLink, existingFile);
		
		if(!Files.exists(newLink)) {
			throw new FileNotFoundException(String.format("File %s failed to create.", newLink.getFileName()));
		}
		
		// Delete original
		Files.delete(existingFile);
	}
	
	
}
